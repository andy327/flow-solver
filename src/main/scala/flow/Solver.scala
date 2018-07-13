package flow

/**
  * Prioritizes the set of possible BoardStates to traverse by extending a Path by one cell, and finds a solution
  * to the Board by searching for a State that completes all Paths and fills the cells in the Board.
  */
trait Solver extends BoardState {

  /**
    * Returns a Seq of potential BoardStates that can result from extending one Path from one color by one cell.
    * Each path can potentially be extended in one of four directions: Up, Down, Left, or Right. The BoardStates
    * will be sorted by number of potential choices; "forced" moves will be returned first. Moves that result in
    * an invalid board state are filtered out. If a forced move is found, none of the other possible next states
    * will be traversed; since forced moves are inevitable, we will instead only consider choices once we are in
    * a state where all the forced moves have been exhausted.
    */
  def nextStates: Seq[BoardState] = ???

  /**
    * Returns the fully colored Board where each color's endpoints are connected via a valid Path.
    */
  def solution: Board[Int] = ???

}
