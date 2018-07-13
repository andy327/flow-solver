package object flow {

  type Board[T] = Map[Pos, T]
  type Color = Char

  sealed trait Direction
  case object Left extends Direction
  case object Right extends Direction
  case object Up extends Direction
  case object Down extends Direction

  object Directions {
    val all: Set[Direction] = Set(Left, Right, Up, Down)
  }

  case class Pos(row: Int, col: Int) {
    def move(dir: Direction): Pos = dir match {
      case Left => Pos(row, col - 1)
      case Right => Pos(row, col + 1)
      case Up => Pos(row - 1, col)
      case Down => Pos(row + 1, col)
    }
  }

  case class Move(color: Color, pos: Pos)

  case class Path(nodes: Seq[Pos]) { // head Position is most recent
    def size: Int = nodes.size
    def latest: Pos = nodes.head
    def endsWith(pos: Pos): Boolean = pos == latest
    def contains(pos: Pos): Boolean = nodes.contains(pos)
    def extend(pos: Pos): Path = Path(pos +: nodes)
  }

  object Path {
    def from(positions: Pos*) = Path(positions)
  }

}
