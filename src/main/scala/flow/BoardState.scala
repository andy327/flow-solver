package flow

/**
  * This trait represents a single state of a Flow game board, with the any number of the colored paths between
  * endpoints partially or fully completed. Each endpoint has a path of cells that extend over adjacent tiles on
  * the board. Each BoardState can generate successive states that result from extending one of the color paths
  * by one adjacent tile, until the board is filled and each pair of endpoints is connected via a legal path.
  */
trait BoardState extends BoardDef { self =>

  /**
    * Positions to be connected for the path to be complete. Set to None if the path is complete.
    */
  type ColorState = Option[(Pos, Pos)]

  /**
    * Identifier for the connected component a blank cell belongs to.
    */
  type Component = Int

  /**
    * Contains the paths completed so far from each endpoint for all colors.
    */
  val colorPaths: Map[Color, (Path, Path)]

  /**
    * Contains a stack of all the moves performed so far leading to this state from the initial Board.
    */
  val moveStack: Seq[Move] = Seq()

  /**
    * Contains the endpoints for each color that remain to be connected by a Path.
    */
  lazy val colorStates: Map[Color, ColorState] = colorPaths.mapValues{ case (pathA, pathB) =>
    (pathA.latest, pathB.latest) match {
      case (endA, endB) if (areNeighbors(endA, endB)) => None
      case (endA, endB) => Some(endA, endB)
    }
  }

  /**
    * Returns true if the color's paths between endpoints have been connected.
    */
  def colorCompleted(color: Color): Boolean = !colorStates(color).isDefined

  /**
    * Contains the color (Int) of each cell in the board in an Option, where empty cells are stored as None.
    */
  override lazy val cells: Board[Option[Color]] = {
    val colorMap: Map[Pos, Color] = colorPaths.mapValues{ case (p1, p2) => p1.nodes.toSet ++ p2.nodes.toSet }
      .flatMap{ case (color, nodes) => nodes.map{ pos => pos -> color } }
      .toMap

    (for {
      row <- 0 until rows
      col <- 0 until cols
      pos = Pos(row, col)
    } yield pos -> colorMap.get(pos)).toMap
  }

  /**
    * Map containing the ColorStates that are not yet path-complete.
    */
  lazy val activeStates: Map[(Pos, Pos), Color] = colorStates.flatMap { case (color: Color, state: ColorState) =>
    (color, state) match {
      case (color, Some(endPts)) => Seq((endPts, color))
      case (_, None) => Seq.empty
    }
  }

  /**
    * Contains the Map of endpoints that are left to extend to complete a Path, along with their color.
    */
  lazy val activePoints: Map[Pos, Color] = activeStates.flatMap {
    case((endPtA, endPtB), color) => Seq((endPtA, color), (endPtB, color))
  }

  /**
    * Returns true if the Position is an active point.
    */
  def isActivePoint(pos: Pos): Boolean = activePoints.contains(pos)

  /**
    * Returns true if each color has a Path connecting its two initial endpoint positions.
    */
  def pathsCompleted: Boolean = colorStates.values forall (!_.isDefined)

  /**
    * Returns true if each cell in the board is participating in a Path.
    */
  def boardFilled: Boolean = cells.values forall (_.isDefined)

  /**
    * Returns true if the board has been fully solved:
    * each cell is participating in a fully solved Path between two color termini.
    */
  def solved: Boolean = boardFilled && pathsCompleted

  /**
    * Determines the connected components of the unfilled cells. Each component is represented by an Int and
    * each cell is represented by an Option[Int], signifying its component if it is unfilled.
    */
  lazy val components: Board[Option[Component]] = {
    val compIds = collection.mutable.Map() ++ emptyCells.zipWithIndex
    var updated = false

    do {
      updated = false
      emptyCells.foreach{ pos1 =>
        neighbors(pos1).filter(emptyCells.contains(_)).foreach{ pos2 =>
          val id1 = compIds(pos1)
          val id2 = compIds(pos2)
          if (id1 < id2) {
            updated = true
            compIds.update(pos2, id1)
          }
        }
      }
    }
    while (updated)

    (for {
      row <- 0 until rows
      col <- 0 until cols
      pos = Pos(row, col)
    } yield pos -> compIds.get(pos)).toMap
  }

  /**
    * Given an endpoint for a Path, returns the Set of possible components that the path can extend into.
    */
  def componentsForPos(pos: Pos): Set[Component] = (neighbors(pos) map (components(_))).flatten

  /**
    * Returns true if there are no dead-end spaces in this State, i.e. an empty space that is surrounded by three
    * (or more) completed path segments or walls. Since there is no way to fill in this cell with a color and
    * still terminate a Path, this State is immediately marked as invalid.
    */
  def doNoDeadEndsExist: Boolean = emptyCells forall { (pos: Pos) =>
    neighbors(pos).count{ pos2: Pos => isEmpty(pos2) || activePoints.contains(pos2) } > 1
  }

  /**
    * Returns true if any of the endpoints of a color's Path are unable to extend into any of the neighboring
    * spaces, i.e. no neighbors are either empty or are an endpoint of the same color.
    */
  def areNoPathsStranded: Boolean = activeStates.keys
    .filterNot{ case (pos1: Pos, pos2: Pos) => areNeighbors(pos1, pos2) } // neighboring endpoints can complete each other
    .flatMap { case (pos1, pos2) => Seq(pos1, pos2) }
    .forall { (pos: Pos) =>
      neighbors(pos) exists isEmpty
    }

  /**
    * Returns true if the endpoints between the two paths for each color have a neighbor belonging to the same
    * connected component. If two endpoints have no neighbors that belong to the same connected component, they
    * will not be able to connect by any path and the state is invalid.
    */
  def areComponentsLegal: Boolean = colorStates forall { case (_, state) =>
    state match {
      case None => true // the color has been path connected
      case Some((endPosA, endPosB)) => (componentsForPos(endPosA) intersect componentsForPos(endPosB)).nonEmpty
    }
  }

  /**
    * Returns true if this State cannot be immediately discounted as a dead State with no valid moves.
    */
  def isValid: Boolean = doNoDeadEndsExist && areNoPathsStranded && areComponentsLegal

  /**
    * Returns a Set of Moves that can be reached by extending one of the color Paths by one legal move.
    */
  lazy val legalMoves: Set[Move] = activeStates.flatMap { case ((endPtA, endPtB), color) =>
    val validCells = (neighbors(endPtA) ++ neighbors(endPtB)) filter isEmpty
    validCells map { pos: Pos => Move(color, pos) }
  }.toSet

  /**
    * Return true if this move is inevitable: either an endpoint has no other options but to move into the given
    * cell, or choosing another path creates a dead-end space. This check helps to prioritize moves that are
    * necessary and reduce the number of choices for the following States. Note that this method does not check
    * whether a given move is the only choice that does not restrict another path from extending or otherwise lead
    * to an invalid state.
    */
  def isMoveForced(nextMove: Move): Boolean = {
    val (pathA, pathB) = colorPaths(nextMove.color)

    // we need to check if the move is forced from either endpoint
    val (prevPosA, prevPosB) = (pathA.latest, pathB.latest)

    // check if the endpoint has only one possible move, or if choosing any other move leaves a neighboring empty cell stranded
    def forcedFromPrevPos(prevPos: Pos): Boolean =
      neighbors(prevPos).filter(isEmpty).size == 1 ||
        neighbors(nextMove.pos).count(pos => isEmpty(pos) || isActivePoint(pos)) == 2

    (areNeighbors(pathA.latest, nextMove.pos) && forcedFromPrevPos(pathA.latest)) ||
      (areNeighbors(pathB.latest, nextMove.pos) && forcedFromPrevPos(pathB.latest))
  }

  /**
    * Returns a copy of this BoardState with the modified color Paths.
    */
  def copyWithNewMove(nextMove: Move): BoardState = {
    val (pathA, pathB) = colorPaths(nextMove.color)
    val newPaths =
      if (areNeighbors(pathA.latest, nextMove.pos)) (pathA.extend(nextMove.pos), pathB)
      else (pathA, pathB.extend(nextMove.pos))

    new BoardState {
      val rows = self.rows
      val cols = self.cols
      val colorEndPts = self.colorEndPts
      val colorPaths = self.colorPaths.updated(nextMove.color, newPaths)
      override val moveStack = nextMove +: self.moveStack
    }
  }


  // Helper methods for state-ranking heuristic function

  /**
    * Store the last cell filled in to prioritize the next move.
    */
  def lastMove: Option[Move] = moveStack.headOption

  /**
    * Returns the color of the last filled-in cell.
    */
  def lastColor: Option[Color] = lastMove.map(_.color)

  /**
    * Returns the position of the last filled-in cell.
    */
  def lastPos: Option[Pos] = lastMove.map(_.pos)

  /**
    * Returns true if the last move extended the Path ending with the previous move.
    */
  def doesLastMoveContinuePath: Boolean = moveStack match {
    case last :: previous :: rest => areNeighbors(last.pos, previous.pos)
    case _ => false
  }

  /**
    * Returns true if the last move borders an earlier move (besides the previous cell) on the same Path (wasteful solution).
    */
  def doesLastMoveFoldPath: Boolean = lastMove match {
    case Some(move) => {
      val (pathA, pathB) = colorPaths(move.color)
      val prevPath = if (pathA.endsWith(move.pos)) pathA else pathB
      prevPath.nodes.count{ pos => areNeighbors(move.pos, pos) } > 1 // should only border the previous cell in the Path
    }
    case None => false
  }

  /**
    * Returns true if the last move completes a border cell.
    */
  def doesLastMoveBorderWall: Boolean = lastPos match {
    case Some(pos) => isBorderCell(pos)
    case None => false
  }


  // Output and debugging functions

  private val segment = " \u25AB "

  private def colorize(color: Option[Color])(str: String) = color match {
    case Some(ch) => s"\u001b[48;5;${ch-47}m${str}\u001b[0m"
    case None => s"\u001b[48;5;0m${str}\u001b[0m"
  }

  override def toString: String = {
    def rowString(r: Int) = (0 until cols).map(c => (c, cells(Pos(r, c)))).map{ case (c, color) => color match {
      case Some(ch) if (endPtMap.contains(Pos(r, c))) => colorize(Some(ch))(s" ${ch} ")
      case _ => colorize(color)(segment)
    }}.mkString("")
    (0 until rows).map(rowString).mkString("\n")
  }

  /**
    * Returns a String representation of the Board along with information on the state itself.
    */
  def debug: String = {
    s"""${toString}
      |board filled: ${boardFilled}
      |paths completed: ${colorStates.values.count(!_.isDefined)}/${colorStates.size}
      |solved: ${solved}
      |dead ends: ${!doNoDeadEndsExist}
      |stranded paths: ${!areNoPathsStranded}
      |invalid components: ${!areComponentsLegal}
      |valid state: ${isValid}""".stripMargin
  }

}

object BoardState {

  /**
    * Return the initial BoardState for an initialized BoardDef.
    */
  def fromBoard(board: BoardDef): BoardState = new BoardState {
    val rows = board.rows
    val cols = board.cols
    val colorEndPts = board.colorEndPts
    val colorPaths = colorEndPts.mapValues{ case (pos1, pos2) => (Path.from(pos1), Path.from(pos2)) }
  }

}