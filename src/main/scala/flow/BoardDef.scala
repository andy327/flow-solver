package flow

/**
  * This trait represents a Flow game board and its initial configuration, consisting of a board with a row by col
  * grid of spaces along with the endpoints for each color that need to be connected by a valid Path.
  */
trait BoardDef {

  val rows, cols: Int

  /**
    * Contains the two starting positions for oppsite ends of the flow for each Color.
    */
  val colorEndPts: Map[Color, (Pos, Pos)]

  def numColors = colorEndPts.size

  /**
    * Map of endpoint positions to color.
    */
  lazy val endPtMap: Map[Pos, Color] = colorEndPts.flatMap{ case (color, (start, end)) =>
    Seq(start -> color, end -> color)
  }

  /**
    * Contains the color (Int) of each cell in the board in an Option, where empty cells are stored as None.
    */
  lazy val cells: Board[Option[Color]] =
    (for {
      row <- 0 until rows
      col <- 0 until cols
      pos = Pos(row, col)
    } yield pos -> endPtMap.get(pos)).toMap

  /**
    * Return a sequence containing all the cells that have not yet been filled in with a color.
    */
  lazy val emptyCells: Seq[Pos] = cells.filter{ case (pos, color) => !color.isDefined }.keys.toSeq

  /**
    * Returns true if the position has been filled in with a color.
    */
  def isEmpty(pos: Pos): Boolean = !cells(pos).isDefined

  /**
    * Returns true if the position borders the edge of the Board.
    */
  def isBorderCell(pos: Pos): Boolean = pos.row == 0 || pos.row == rows - 1 || pos.col == 0 || pos.col == cols - 1

  /**
    * Returns true if the given position is within the boundaries of the Board.
    */
  def validPos(pos: Pos): Boolean = (0 <= pos.row && pos.row < rows) && (0 <= pos.col && pos.col < cols)

  /**
    * Returns an Option containing the position after traversing one cell in the given direction, if the cell
    * is valid.
    */
  def neighborPos(pos: Pos)(dir: Direction): Option[Pos] = Option(pos move dir) filter validPos

  /**
    * Returns the Set of all valid neighboring positions for a given position.
    */
  def neighbors(pos: Pos): Set[Pos] = (Directions.all map neighborPos(pos)).flatten

  /**
    * Returns true if the two positions are neighbors.
    */
  def areNeighbors(pos1: Pos, pos2: Pos): Boolean = neighbors(pos1) contains pos2

  override def toString: String = {
    def rowString(r: Int) = (0 until cols).map(c => cells(Pos(r, c))).map{ _ match {
      case Some(i) => s"${i}"
      case None => "-"
    }}.mkString(" ")
    (0 until rows).map(rowString).mkString("\n")
  }

}
