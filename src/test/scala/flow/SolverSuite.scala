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

  object Board4 extends StringParserBoard with Solver {
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

  test("nextStates") {
    assert(Board1.solution.solved)
    println(s"iterations for board 1: ${Board1.numIterations}")
    assert(Board2.solution.solved)
    println(s"iterations for board 2: ${Board2.numIterations}")
    assert(Board3.solution.solved)
    println(s"iterations for board 3: ${Board3.numIterations}")
    // assert(Board4.solution.solved)
    // println(s"iterations for board 4: ${Board4.numIterations}")
  }

}
