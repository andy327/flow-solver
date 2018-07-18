package flow

import org.scalatest.FunSuite

class SolverSuite extends FunSuite {

  object Board5x5 extends StringParserBoard with Solver {
    val board =
      """0----
        |-----
        |--1--
        |213-0
        |3---2""".stripMargin
  }

  object Board6x6 extends StringParserBoard with Solver {
    val board =
      """-----0
        |------
        |0-----
        |---1--
        |-12---
        |3---23""".stripMargin
  }

  object Board7x7 extends StringParserBoard with Solver {
    val board =
      """-------
        |01---23
        |40----5
        |--1----
        |-----2-
        |---4-3-
        |5------""".stripMargin
  }

  object Board8x8 extends StringParserBoard with Solver { // very difficult board
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

  object Board9x9 extends StringParserBoard with Solver {
    val board =
      """0--------
        |---------
        |---1-----
        |--2--3---
        |-----4---
        |0-34-5---
        |6--7-----
        |-5---6-2-
        |-------17""".stripMargin
  }

  object Board10x10 extends StringParserBoard with Solver {
    val board =
      """----------
        |----------
        |-------0--
        |-10-----1-
        |-2---34-5-
        |-----6----
        |--746-----
        |---------8
        |-3-2-----5
        |-8-------7""".stripMargin
  }

  test("solving a 5x5 board") {
    assert(Board5x5.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 6x6 board") {
    assert(Board6x6.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 7x7 board") {
    assert(Board7x7.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 8x8 board") {
    assert(Board8x8.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 9x9 board") {
    assert(Board9x9.solution(maxIterations = 1000).isDefined)
  }

  test("solving a 10x10 board") {
    assert(Board10x10.solution(maxIterations = 1000).isDefined)
  }

}
