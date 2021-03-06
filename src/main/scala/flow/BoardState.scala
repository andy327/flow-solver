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
    * When creating a new BoardState, we store whether the move was forced so we can keep count of the number of
    * forced moves.
    */
  val isForced: Boolean = false

  /**
    * The number of forced moves is a useful metric for assigning a heuristic score to BoardStates.
    */
  val numForcedMovesSoFar: Int = 0

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
    * The Path that ends with the latest Move.
    */
  lazy val lastExtendedPath: Path = lastMove.map{ move =>
    val (pathA, pathB) = colorPaths(move.color)
    if (pathA.endsWith(move.pos)) pathA else pathB
  }.getOrElse(Path.empty)

  /**
    * Returns true if the Position is an active point.
    */
  def isActivePoint(pos: Pos): Boolean = activePoints.contains(pos)

  /**
    * Returns true if each color has a Path connecting its two initial endpoint positions.
    */
  lazy val allPathsCompleted: Boolean = colorStates.values forall (!_.isDefined)

  /**
    * Returns the number of flows which have complete and valid Paths.
    */
  lazy val numPathsCompleted: Int = colorStates.values.count(!_.isDefined)

  /**
    * Returns true if each cell in the board is participating in a Path.
    */
  lazy val boardFilled: Boolean = cells.values forall (_.isDefined)

  /**
    * Returns true if the board has been fully solved:
    * each cell is participating in a fully solved Path between two color termini.
    */
  lazy val solved: Boolean = boardFilled && allPathsCompleted

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
        neighbors(pos1).filter(emptyCells.contains(_)) foreach { pos2 =>
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
    * Returns true if there are no dead-end spaces in this State. There are two types of dead spaces: (1) empty cells that
    * are surrounded by three (or more) completed path segments or walls, and (2) empty cells that are surrounded by no
    * empty spaces, and no two active points of the same color. Since there is no way to fill in this cell with a color and
    * still terminate a Path, this State is immediately marked as invalid. It suffices to check only the empty cells bordering
    * the last Move for dead-ends.
    */
  lazy val doNoDeadEndsExist: Boolean = lastMove match {
    case None => true
    case Some(move) => neighbors(move.pos).filter(isEmpty) forall { (posToCheck: Pos) =>
      val validNeighbors = neighbors(posToCheck)
      val notDeadEndType1 = validNeighbors.count{ pos: Pos => isEmpty(pos) || isActivePoint(pos) } > 1
      val notDeadEndType2 = (validNeighbors.exists(isEmpty)) || {
        validNeighbors.filter(isActivePoint)
          .groupBy(pos => activePoints(pos))
          .exists{ case (color, pts) => pts.size == 2 }
      }
      notDeadEndType1 && notDeadEndType2
    }
  }

  /**
    * Returns true if any of the endpoints of a color's Path are unable to extend into any of the neighboring
    * spaces, i.e. no neighbors are either empty or are an endpoint of the same color.
    */
  lazy val areNoPathsStranded: Boolean = activeStates.keys
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
  lazy val areComponentsLegal: Boolean = colorStates forall { case (_, state) =>
    state match {
      case None => true // the color has been path connected
      case Some((endPosA, endPosB)) => (componentsForPos(endPosA) intersect componentsForPos(endPosB)).nonEmpty
    }
  }

  /**
    * Returns true if each region of empty cells has at least one pair of active points bordering them. If this is not the
    * case, the empty cells cannot be filled in while still connecting each pair of endpoints, and the state is invalid.
    */
  lazy val areNoRegionsStranded: Boolean = {
    val regions: Seq[Set[Pos]] = components
      .groupBy(_._2)
      .filterKeys(_.isDefined)
      .map{ case (_, componentIds) => componentIds.keys.toSet }.toList

    val regionColors: Seq[Seq[Color]] = regions.map{
      set => set.flatMap(neighbors).filter(isActivePoint).toSeq.map(activePoints)
    }

    regionColors.forall(colors => colors.distinct.size != colors.size)
  }

  /**
    * Returns the number of Paths that are impossible to connect. This method is used to ensure that the width of a
    * chokepoint is larger than the number of Paths that need to cross it.
    */
  def numBrokenPaths(ignoredColor: Color): Int = {
    colorStates count { case (color, state) =>
      state match {
        case None => false
        case _ if (color == ignoredColor) => false
        case Some((endPosA, endPosB)) => (componentsForPos(endPosA) intersect componentsForPos(endPosB)).isEmpty
      }
    }
  }

  /**
    * Returns true if the last Move caused a chokepoint, i.e. a narrow passage of width W where at least W + 1 paths
    * must cross in order to reach their respective endpoints. This is accomplished by extending the path forward
    * until it reaches a non-empty cell. The resulting extension divides the region into two sub-regions, and we check
    * to see if the number of endpoints that are not bordering a shared connected component of empty regions is greater
    * than the number of cells we extended past the last move.
    */
  lazy val areThereNoChokepoints: Boolean = lastMove match {
    case None => true
    case Some(move) if (isBorderCell(move.pos)) => true
    case Some(move) => { // create the state where we extend the path till it reaches a non-empty cell
      val extendedMoves: Seq[Move] = {
        val prevPos = lastExtendedPath.nodes(1) // an extended Path will always be at least two Moves long
        val newDirection = move.pos.directionFromPos(prevPos)

        lazy val extension: Stream[Pos] = Stream.cons(move.pos, extension.map(_.move(newDirection)))
        val extendedPositions: Seq[Pos] = extension.tail.takeWhile(pos => validPos(pos) && isEmpty(pos)).toList

        extendedPositions.map(pos => Move(move.color, pos))
      }

      val chokepointWidth = extendedMoves.length
      val extendedState = extendedMoves.foldLeft(self){ case (state, move) => state.copyWithNewMove(move) }

      // we ignore the color of the extended path in our chokepoint calculation
      extendedState.numBrokenPaths(ignoredColor = move.color) <= chokepointWidth
    }
  }

  /**
    * Returns true if this State cannot be immediately discounted as a dead State with no valid moves.
    */
  lazy val isValid: Boolean = doNoDeadEndsExist && areNoPathsStranded && areNoPathsFolded &&
    areComponentsLegal && areNoRegionsStranded && areThereNoChokepoints

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
    val isNextMoveForced = isMoveForced(nextMove)
    val forcedMoveBoost = if (isNextMoveForced) 1 else 0

    new BoardState {
      val rows = self.rows
      val cols = self.cols
      val colorEndPts = self.colorEndPts
      val colorPaths = self.colorPaths.updated(nextMove.color, newPaths)
      override val moveStack = nextMove +: self.moveStack
      override val isForced = isNextMoveForced
      override val numForcedMovesSoFar = self.numForcedMovesSoFar + forcedMoveBoost
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
    * Returns the number of cells remaining to fill.
    */
  lazy val numEmptyCells: Int = emptyCells.size

  /**
    * Returns true if the last move extended the Path ending with the previous move.
    */
  lazy val doesLastMoveContinuePath: Boolean = moveStack match {
    case last :: previous :: rest => areNeighbors(last.pos, previous.pos)
    case _ => false
  }

  /**
    * Returns true if the last move borders an earlier move (besides the previous cell) on the same Path (wasteful solution).
    */
  lazy val areNoPathsFolded: Boolean = lastMove match {
    case Some(move) => { // should only border the previous cell in the Path
      val (pathA, pathB) = colorPaths(move.color)
      val pathSegments = pathA.inactive ++ pathB.inactive
      pathSegments.count{ pos => areNeighbors(move.pos, pos) } <= 1
    }
    case None => true
  }

  /**
    * Returns true if the last move completes a border cell.
    */
  lazy val doesLastMoveBorderWall: Boolean = lastPos match {
    case Some(pos) => isBorderCell(pos)
    case None => false
  }

  /**
    * Returns the minimum distance to one of the four walls. We can ignore the case where there are no moves because we
    * will never need to consider the initial state as a possible move.
    */
  lazy val lastMoveDistanceToWall: Int = lastPos match {
    case None => 0
    case Some(Pos(r,c)) => Seq(r, (rows - 1) - r, c, (cols - 1) - c).min
  }

  /**
    * Given the Path from the previous State before applying the latest Move, return the number of options we had to extend
    * that Path.
    */
  lazy val lastMovePossibleOptions: Int = lastMove match {
    case None => 1
    case _ => (neighbors(lastExtendedPath.nodes(1)) count isEmpty) + 1
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
      |total moves: ${moveStack.size}
      |number of forced moves: ${numForcedMovesSoFar}/${moveStack.size}
      |board filled: ${boardFilled}
      |paths completed: ${numPathsCompleted}/${colorStates.size}
      |solved: ${solved}
      |dead ends: ${!doNoDeadEndsExist}
      |stranded paths: ${!areNoPathsStranded}
      |folded paths: ${!areNoPathsFolded}
      |invalid components: ${!areComponentsLegal}
      |chokepoints: ${!areThereNoChokepoints}
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
