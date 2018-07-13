package flow

import org.scalatest.FunSuite

class BoardDefSuite extends FunSuite {

  trait Board1 extends BoardDef with StringParserBoard {
    val board =
      """0----
        |-----
        |--1--
        |213-0
        |3---2""".stripMargin
  }

  trait Board2 extends BoardDef with StringParserBoard {
    val board =
      """0-1
        |10-
        |---
        |2-2""".stripMargin
  }

  test("rows and cols") {
    new Board1 {
      assert(rows === 5)
      assert(cols === 5)
    }

    new Board2 {
      assert(rows === 4)
      assert(cols === 3)
    }
  }

  test("numColors") {
    new Board1 {
      assert(numColors === 4)
    }

    new Board2 {
      assert(numColors === 3)
    }
  }

  test("emptyCells") {
    new Board1 {
      assert(emptyCells.toSet === Set(
        Pos(0,2), Pos(4,1), Pos(2,0), Pos(0,3), Pos(1,1), Pos(1,4),
        Pos(0,4), Pos(1,3), Pos(4,2), Pos(2,4), Pos(0,1), Pos(3,3),
        Pos(2,3), Pos(1,2), Pos(2,1), Pos(4,3), Pos(1,0)
      ))
    }

    new Board2 {
      assert(emptyCells.toSet === Set(
        Pos(3,1), Pos(2,0), Pos(2,2), Pos(0,1), Pos(1,2), Pos(2,1)
      ))
    }
  }

  test("isEmpty") {
    new Board1 {
      assert(!isEmpty(Pos(0,0)))
      assert(isEmpty(Pos(0,1)))
      assert(isEmpty(Pos(0,4)))
      assert(isEmpty(Pos(2,0)))
      assert(!isEmpty(Pos(2,2)))
      assert(!isEmpty(Pos(4,0)))
      assert(!isEmpty(Pos(4,4)))
    }

    new Board2 {
      assert(!isEmpty(Pos(0,0)))
      assert(isEmpty(Pos(0,1)))
      assert(!isEmpty(Pos(0,2)))
      assert(!isEmpty(Pos(3,2)))
    }
  }

  test("isBorderCell") {
    new Board1 {
      assert(isBorderCell(Pos(0,0)))
      assert(!isBorderCell(Pos(1,1)))
      assert(isBorderCell(Pos(4,2)))
    }

    new Board2 {
      assert(!isBorderCell(Pos(1,1)))
      assert(!isBorderCell(Pos(-1,1)))
      assert(isBorderCell(Pos(3,2)))
    }
  }

  test("validPos") {
    new Board1 {
      assert(validPos(Pos(0,0)))
      assert(!validPos(Pos(-1,0)))
      assert(validPos(Pos(0,4)))
      assert(!validPos(Pos(4,5)))
    }

    new Board2 {
      assert(validPos(Pos(0,0)))
      assert(!validPos(Pos(0,-1)))
      assert(validPos(Pos(3,2)))
      assert(!validPos(Pos(2,3)))
    }
  }

  test("neighborPos") {
    new Board1 {
      assert(neighborPos(Pos(0,0))(Up) === None)
      assert(neighborPos(Pos(0,0))(Right) === Some(Pos(0,1)))
      assert(neighborPos(Pos(4,1))(Left) === Some(Pos(4,0)))
      assert(neighborPos(Pos(4,1))(Up) === Some(Pos(3,1)))
      assert(neighborPos(Pos(4,1))(Down) === None)
      assert(neighborPos(Pos(4,1))(Up) === Some(Pos(3,1)))
    }

    new Board2 {
      assert(neighborPos(Pos(0,2))(Up) === None)
      assert(neighborPos(Pos(0,2))(Left) === Some(Pos(0,1)))
      assert(neighborPos(Pos(0,2))(Right) === None)
      assert(neighborPos(Pos(2,2))(Up) === Some(Pos(1,2)))
      assert(neighborPos(Pos(2,2))(Right) === None)
    }
  }

  test("neighbors") {
    new Board1 {
      assert(neighbors(Pos(0,0)).toSet === Set(Pos(0,1), Pos(1,0)))
      assert(neighbors(Pos(2,2)).toSet === Set(Pos(1,2), Pos(2,1), Pos(2,3), Pos(3,2)))
      assert(neighbors(Pos(4,3)).toSet === Set(Pos(3,3), Pos(4,2), Pos(4,4)))
    }

    new Board2 {
      assert(neighbors(Pos(0,1)).toSet === Set(Pos(0,0), Pos(0,2), Pos(1,1)))
      assert(neighbors(Pos(3,1)).toSet === Set(Pos(2,1), Pos(3,0), Pos(3,2)))
    }
  }

  test("areNeighbors") {
    new Board1 {
      assert(areNeighbors(Pos(0,2), Pos(0,3)))
      assert(!areNeighbors(Pos(0,0), Pos(0,-1)))
      assert(!areNeighbors(Pos(-1,0), Pos(2,2)))
    }

    new Board2 {
      assert(areNeighbors(Pos(0,0), Pos(0,1)))
      assert(!areNeighbors(Pos(0,0), Pos(1,1)))
      assert(!areNeighbors(Pos(3,2), Pos(3,3)))
    }
  }

}
