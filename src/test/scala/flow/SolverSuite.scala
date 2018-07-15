package flow

import org.scalatest.FunSuite

class SolverSuite extends FunSuite {

  object Board1 extends StringParserBoard with Solver {
    val board =
      """0----
        |-----
        |--1--
        |213-0
        |3---2""".stripMargin
  }

  object Board2 extends StringParserBoard with Solver {
    val board =
      """-----0
        |------
        |0-----
        |---1--
        |-12---
        |3---23""".stripMargin
  }

  object Board3 extends StringParserBoard with Solver {
    val board =
      """-------
        |01---23
        |40----5
        |--1----
        |-----2-
        |---4-3-
        |5------""".stripMargin
  }

  object Board4 extends StringParserBoard with Solver { // very difficult board
    val board =
      """01-----2
        |------3-
        |----1---
        |-----4--
        |--3--0-4
        |--2--5--
        |------5-
        |--------""".stripMargin
  }

  test("solving a 5x5 board") {
    assert(Board1.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 6x6 board") {
    assert(Board2.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 7x7 board") {
    assert(Board3.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 8x8 board") {
    assert(Board4.solution(maxIterations = 1000).isDefined)
  }

}
