package flow

import org.scalatest.FunSuite

class BoardStateSuite extends FunSuite {

  // 0 - - - - (initial state)
  // - - - - -
  // - - 1 - -
  // 2 1 3 - 0
  // 3 - - - 2
  trait Board1 extends BoardState {
    val rows = 5
    val cols = 5
    val colorEndPts = Map(
      '0' -> (Pos(0,0), Pos(3,4)),
      '1' -> (Pos(2,2), Pos(3,1)),
      '2' -> (Pos(3,0), Pos(4,4)),
      '3' -> (Pos(3,2), Pos(4,0))
    )
    val colorPaths = colorEndPts.mapValues{ case (pos1, pos2) => (Path.from(pos1), Path.from(pos2)) }
  }

  // 0 0 0 0 0 (incomplete but valid)
  // - - - - 0
  // 2 - 1 - -
  // 2 1 3 - 0
  // 3 3 3 2 2
  trait Board2 extends Board1 {
    override val colorPaths = Map(
      '0' -> (Path.from(Pos(1,4), Pos(0,4), Pos(0,3), Pos(0,2), Pos(0,1), Pos(0,0)), Path.from(Pos(3,4))),
      '1' -> (Path.from(Pos(2,2)), Path.from(Pos(3,1))),
      '2' -> (Path.from(Pos(2,0), Pos(3,0)), Path.from(Pos(4,3), Pos(4,4))),
      '3' -> (Path.from(Pos(4,1), Pos(4,2), Pos(3,2)), Path.from(Pos(4,0)))
    )
    override val moveStack = Seq(
      Move('2', Pos(4,3)),
      Move('2', Pos(2,0)),
      Move('3', Pos(4,2)),
      Move('3', Pos(4,1)),
      Move('0', Pos(1,4)),
      Move('0', Pos(0,4)),
      Move('0', Pos(0,3)),
      Move('0', Pos(0,2)),
      Move('0', Pos(0,1))
    )
  }

  // 0 0 - 0 - (dead-end)
  // - 0 0 0 -
  // - - 1 - -
  // 2 1 3 - 0
  // 3 - - - 2
  trait Board3 extends Board1 {
    override val colorPaths = Map(
      '0' -> (Path.from(Pos(0,3), Pos(1,3), Pos(1,2), Pos(1,1), Pos(0,1), Pos(0,0)), Path.from(Pos(3,4))),
      '1' -> (Path.from(Pos(2,2)), Path.from(Pos(3,1))),
      '2' -> (Path.from(Pos(3,0)), Path.from(Pos(4,4))),
      '3' -> (Path.from(Pos(3,2)), Path.from(Pos(4,0)))
    )
    override val moveStack = Seq(
      Move('0', Pos(0,3)),
      Move('0', Pos(1,3)),
      Move('0', Pos(1,2)),
      Move('0', Pos(1,1)),
      Move('0', Pos(0,1))
    )
  }

  // 0 - - - - (stranded path)
  // - - - - -
  // - - 1 - -
  // 2 1 3 - 0
  // 3 1 - - 2
  trait Board4 extends Board1 {
    override val colorPaths = Map(
      '0' -> (Path.from(Pos(0,0)), Path.from(Pos(3,4))),
      '1' -> (Path.from(Pos(2,2)), Path.from(Pos(4,1), Pos(3,1))),
      '2' -> (Path.from(Pos(3,0)), Path.from(Pos(4,4))),
      '3' -> (Path.from(Pos(3,2)), Path.from(Pos(4,0)))
    )
    override val moveStack = Seq(
      Move('1', Pos(4,1))
    )
  }

  // 0 - - - - (illegal components)
  // - - - - -
  // - - 1 1 -
  // 2 1 3 - 0
  // 3 - - - 2
  trait Board5 extends Board1 {
    override val colorPaths = Map(
      '0' -> (Path.from(Pos(0,0)), Path.from(Pos(3,4))),
      '1' -> (Path.from(Pos(2,3), Pos(2,2)), Path.from(Pos(3,1))),
      '2' -> (Path.from(Pos(3,0)), Path.from(Pos(4,4))),
      '3' -> (Path.from(Pos(3,2)), Path.from(Pos(4,0)))
    )
    override val moveStack = Seq(
      Move('1', Pos(2,3))
    )
  }

  // 0 0 0 0 0 (solved)
  // 2 2 2 2 0
  // 2 1 1 2 0
  // 2 1 3 2 0
  // 3 3 3 2 2
  trait Board6 extends Board1 {
    override val colorPaths = Map(
      '0' -> (Path.from(Pos(2,4), Pos(1,4), Pos(0,4), Pos(0,3), Pos(0,2), Pos(0,1), Pos(0,0)), Path.from(Pos(3,4))),
      '1' -> (Path.from(Pos(2,1), Pos(2,2)), Path.from(Pos(3,1))),
      '2' -> (Path.from(Pos(4,3), Pos(3,3), Pos(2,3), Pos(1,3), Pos(1,2), Pos(1,1), Pos(1,0), Pos(2,0), Pos(3,0)), Path.from(Pos(4,4))),
      '3' -> (Path.from(Pos(4,1), Pos(4,2), Pos(3,2)), Path.from(Pos(4,0)))
    )
    override val moveStack = Seq(
      Move('2', Pos(4,3)),
      Move('2', Pos(3,3)),
      Move('1', Pos(2,1)),
      Move('2', Pos(2,3)),
      Move('2', Pos(1,3)),
      Move('2', Pos(1,2)),
      Move('2', Pos(1,1)),
      Move('2', Pos(1,0)),
      Move('2', Pos(2,0)),
      Move('3', Pos(4,2)),
      Move('3', Pos(4,1)),
      Move('0', Pos(1,4)),
      Move('0', Pos(0,4)),
      Move('0', Pos(2,4)),
      Move('0', Pos(0,3)),
      Move('0', Pos(0,2)),
      Move('0', Pos(0,1))
    )
  }

  // 3 - - - (chokepoint)
  // 2 - - -
  // 1 - - -
  // 0 0 - -
  // - 1 - -
  // - 2 - -
  // 0 3 - -
  trait Board7 extends BoardState {
    val rows = 7
    val cols = 4
    val colorEndPts = Map(
      '0' -> (Pos(3,0), Pos(6,0)),
      '1' -> (Pos(2,0), Pos(4,1)),
      '2' -> (Pos(1,0), Pos(5,1)),
      '3' -> (Pos(0,0), Pos(6,1))
    )
    override val colorPaths = Map(
      '0' -> (Path.from(Pos(3,1), Pos(3,0)), Path.from(Pos(6,0))),
      '1' -> (Path.from(Pos(2,0)), Path.from(Pos(4,1))),
      '2' -> (Path.from(Pos(1,0)), Path.from(Pos(5,1))),
      '3' -> (Path.from(Pos(0,0)), Path.from(Pos(6,1)))
    )
    override val moveStack = Seq(
      Move('0', Pos(3,1))
    )
  }

  test("allPathsCompleted") {
    new Board1 { assert(!allPathsCompleted) }
    new Board2 { assert(!allPathsCompleted) }
    new Board3 { assert(!allPathsCompleted) }
    new Board4 { assert(!allPathsCompleted) }
    new Board5 { assert(!allPathsCompleted) }
    new Board6 { assert(allPathsCompleted) }
    new Board7 { assert(!allPathsCompleted) }
  }

  test("boardFilled") {
    new Board1 { assert(!boardFilled) }
    new Board2 { assert(!boardFilled) }
    new Board3 { assert(!boardFilled) }
    new Board4 { assert(!boardFilled) }
    new Board5 { assert(!boardFilled) }
    new Board6 { assert(boardFilled) }
    new Board7 { assert(!boardFilled) }
  }

  test("solved") {
    new Board1 { assert(!solved) }
    new Board2 { assert(!solved) }
    new Board3 { assert(!solved) }
    new Board4 { assert(!solved) }
    new Board5 { assert(!solved) }
    new Board6 { assert(solved) }
    new Board7 { assert(!solved) }
  }

  test("doNoDeadEndsExist") {
    new Board1 { assert(doNoDeadEndsExist) }
    new Board2 { assert(doNoDeadEndsExist) }
    new Board3 { assert(!doNoDeadEndsExist) }
    new Board4 { assert(doNoDeadEndsExist) }
    new Board5 { assert(doNoDeadEndsExist) }
    new Board6 { assert(doNoDeadEndsExist) }
    new Board7 { assert(doNoDeadEndsExist) }
  }

  test("areNoPathsStranded") {
    new Board1 { assert(areNoPathsStranded) }
    new Board2 { assert(areNoPathsStranded) }
    new Board3 { assert(areNoPathsStranded) }
    new Board4 { assert(!areNoPathsStranded) }
    new Board5 { assert(areNoPathsStranded) }
    new Board6 { assert(areNoPathsStranded) }
    new Board7 { assert(areNoPathsStranded) }
  }

  test("areComponentsLegal") {
    new Board1 { assert(areComponentsLegal) }
    new Board2 { assert(areComponentsLegal) }
    new Board3 { assert(!areComponentsLegal) }
    new Board4 { assert(!areComponentsLegal) }
    new Board5 { assert(!areComponentsLegal) }
    new Board6 { assert(areComponentsLegal) }
    new Board7 { assert(!areComponentsLegal) }
  }

  test("areNoRegionsStranded") {
    new Board1 { assert(areNoRegionsStranded) }
    new Board2 { assert(areNoRegionsStranded) }
    new Board3 { assert(!areNoRegionsStranded) }
    new Board4 { assert(areNoRegionsStranded) }
    new Board5 { assert(areNoRegionsStranded) }
    new Board6 { assert(areNoRegionsStranded) }
    new Board7 { assert(!areNoRegionsStranded) }
  }

  test("areThereNoChokepoints") {
    new Board1 { assert(areThereNoChokepoints) }
    new Board2 { assert(areThereNoChokepoints) }
    new Board3 { assert(areThereNoChokepoints) }
    new Board4 { assert(areThereNoChokepoints) }
    new Board5 { assert(!areThereNoChokepoints) }
    new Board6 { assert(areThereNoChokepoints) }
    new Board7 { assert(!areThereNoChokepoints) }
  }

  test("isValid") {
    new Board1 { assert(isValid) }
    new Board2 { assert(isValid) }
    new Board3 { assert(!isValid) }
    new Board4 { assert(!isValid) }
    new Board5 { assert(!isValid) }
    new Board6 { assert(isValid) }
    new Board7 { assert(!isValid) }
  }

  test("legalMoves") {
    new Board1 { assert(legalMoves.size === 13) }
    new Board2 { assert(legalMoves.size === 9) }
    new Board3 { assert(legalMoves.size === 12) }
    new Board4 { assert(legalMoves.size === 12) }
    new Board5 { assert(legalMoves.size === 14) }
    new Board6 { assert(legalMoves.size === 0) }
    new Board7 { assert(legalMoves.size === 11) }
  }

  test("isMoveForced") {
    new Board1 { assert(legalMoves.count(isMoveForced) == 3) }
    new Board2 { assert(legalMoves.count(isMoveForced) == 3) }
    new Board3 { assert(legalMoves.count(isMoveForced) == 4) }
    new Board4 { assert(legalMoves.count(isMoveForced) == 3) }
    new Board5 { assert(legalMoves.count(isMoveForced) == 3) }
    new Board6 { assert(legalMoves.count(isMoveForced) == 0) }
    new Board7 { assert(legalMoves.count(isMoveForced) == 6) }
  }

  test("numEmptyCells") {
    new Board1 { assert(numEmptyCells == 17) }
    new Board2 { assert(numEmptyCells == 8) }
    new Board3 { assert(numEmptyCells == 12) }
    new Board4 { assert(numEmptyCells == 16) }
    new Board5 { assert(numEmptyCells == 16) }
    new Board6 { assert(numEmptyCells == 0) }
    new Board7 { assert(numEmptyCells == 19) }
  }

  test("doesLastMoveContinuePath") {
    new Board1 { assert(!doesLastMoveContinuePath) }
    new Board2 { assert(!doesLastMoveContinuePath) }
    new Board3 { assert(doesLastMoveContinuePath) }
    new Board4 { assert(!doesLastMoveContinuePath) }
    new Board5 { assert(!doesLastMoveContinuePath) }
    new Board6 { assert(doesLastMoveContinuePath) }
    new Board7 { assert(!doesLastMoveContinuePath) }
  }

  test("areNoPathsFolded") {
    new Board1 { assert(areNoPathsFolded) }
    new Board2 { assert(areNoPathsFolded) }
    new Board3 { assert(areNoPathsFolded) }
    new Board4 { assert(areNoPathsFolded) }
    new Board5 { assert(areNoPathsFolded) }
    new Board6 { assert(areNoPathsFolded) }
    new Board7 { assert(areNoPathsFolded) }
  }

  test("doesLastMoveBorderWall") {
    new Board1 { assert(!doesLastMoveBorderWall) }
    new Board2 { assert(doesLastMoveBorderWall) }
    new Board3 { assert(doesLastMoveBorderWall) }
    new Board4 { assert(doesLastMoveBorderWall) }
    new Board5 { assert(!doesLastMoveBorderWall) }
    new Board6 { assert(doesLastMoveBorderWall) }
    new Board7 { assert(!doesLastMoveBorderWall) }
  }

  test("lastMoveDistanceToWall") {
    new Board1 { assert(lastMoveDistanceToWall == 0) }
    new Board2 { assert(lastMoveDistanceToWall == 0) }
    new Board3 { assert(lastMoveDistanceToWall == 0) }
    new Board4 { assert(lastMoveDistanceToWall == 0) }
    new Board5 { assert(lastMoveDistanceToWall == 1) }
    new Board6 { assert(lastMoveDistanceToWall == 0) }
    new Board7 { assert(lastMoveDistanceToWall == 1) }
  }

  test("lastMovePossibleOptions") {
    new Board1 { assert(lastMovePossibleOptions == 1) }
    new Board2 { assert(lastMovePossibleOptions == 1) }
    new Board3 { assert(lastMovePossibleOptions == 3) }
    new Board4 { assert(lastMovePossibleOptions == 2) }
    new Board5 { assert(lastMovePossibleOptions == 3) }
    new Board6 { assert(lastMovePossibleOptions == 1) }
    new Board7 { assert(lastMovePossibleOptions == 2) }
  }

}
