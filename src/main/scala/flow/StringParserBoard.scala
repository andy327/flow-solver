package flow

/**
  * Initializes a Flow game board by parsing a board string containing color path endpoints and empty cells.
  * Each cell is denoted by either a '-' signifying an empty cell, or a Char representing one of two path
  * end-points for a color.
  */
trait StringParserBoard extends BoardDef {

  /**
    * An ASCII representation of the Board.
    */
  val board: String

  lazy val rows: Int = vector.size
  lazy val cols: Int = vector.head.size

  lazy val colorEndPts: Map[Color, (Pos, Pos)] = {
    val endPtMap: Map[Color, Seq[Pos]] = endPts.groupBy(_._2)
      .map{ case (color, seq) => color -> seq.map(_._1) }

    endPtMap.foreach{ case (color, seq) =>
      assert(seq.size == 2, s"did not find exactly two endpoints for color: ${color}")
    }

    endPtMap.mapValues{ seq => (seq(0), seq(1)) }
  }

  private lazy val vector: Vector[Vector[Char]] =
    Vector(board.split("\n").map(row => Vector(row: _*)): _*)

  private lazy val endPts: Seq[(Pos, Color)] = for {
    row <- 0 until rows
    col <- 0 until cols
    cell = vector(row)(col)
    if (cell != '-')
    pos = Pos(row, col)
  } yield pos -> cell

}
