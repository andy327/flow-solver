package flow

import scala.collection.mutable.PriorityQueue

/**
  * Prioritizes the set of possible BoardStates to traverse by extending a Path by one cell, and finds a solution
  * to the Board by searching for a State that completes all Paths and fills the cells in the Board. This trait
  * uses a priority queue to store BoardStates and ranks them according to a heuristic function.
  */
trait Solver extends BoardDef { self =>

  /**
    * State container containing a BoardState and the value of its heuristic function.
    */
  case class ScoredState(state: BoardState, score: Double)

  /**
    * Function that assigns a score to a BoardState, where a higher value is more likely to approach a solution.
    */
  def heuristic: BoardState => Double = state => 0.0 +
    (-1.0) * state.numberOfEmptyCells +
    (1.0) * state.numPathsCompleted +
    (0.9) * state.doesLastMoveContinuePath.compare(false) +
    (-100.0) * state.doesLastMoveFoldPath.compare(false) +
    (0.5) * state.doesLastMoveBorderWall.compare(false) +
    (0.0) * state.lastMoveDistanceToWall

  /**
    * BoardStates with a higher score of their heuristic function will be explored first.
    */
  val queue: PriorityQueue[ScoredState] = PriorityQueue()(Ordering.by(_.score))

  var numIterations: Int = 0

  def enqueue(state: BoardState): Unit = queue.enqueue(ScoredState(state, heuristic(state)))
  def dequeue(): ScoredState = queue.dequeue

  def init(): Unit = {
    clear()
    enqueue(BoardState.fromBoard(self))
  }

  def clear(): Unit = queue.clear()

  /**
    * Returns a Seq of potential BoardStates that can result from extending one Path from one color by one cell.
    * Each path can potentially be extended in one of four directions: Up, Down, Left, or Right. The BoardStates
    * will be sorted by number of potential choices; "forced" moves will be returned first. Moves that result in
    * an invalid board state are filtered out. If a forced move is found, none of the other possible next states
    * will be returned; since forced moves are inevitable, we will instead only consider choices once we are in
    * a state where all the forced moves have been exhausted.
    */
  def nextStates(state: BoardState): Seq[BoardState] = {
    state.legalMoves.find(state.isMoveForced) match {
      case Some(move) => Seq(state.copyWithNewMove(move))
      case None => state.legalMoves
        .map(state.copyWithNewMove).toSeq
        .filter(_.isValid)
        .sortBy(_.lastMovePossibleOptions)
    }
  }

  /**
    * Returns the fully solved BoardState where each color's endpoints are connected via a valid Path.
    */
  def solution: BoardState = {
    init()
    var next: ScoredState = dequeue()
    numIterations = 0
    while (!next.state.solved && numIterations < 2000) {
      nextStates(next.state).map(enqueue)
      next = dequeue()
      // println()
      // println(s"SCORE: ${next.score}")
      // println(next.state.debug)
      numIterations += 1
    }
    next.state
  }

}
