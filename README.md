
# Flow Free Solver

This repository contains a solver for puzzles from the [Flow Free](https://www.bigduckgames.com/flowfree) puzzle game for iOS, Android and PC. The objective of Flow Free is to connect matching colors with pipe, while making sure to fill the entire board without crossing any paths. Flow Free is based on the popular logic puzzle [Numberlink](https://en.wikipedia.org/wiki/Numberlink), of which finding a solution has been shown to be [NP-complete](https://en.wikipedia.org/wiki/NP-completeness)<sup>[[1]](https://ci.nii.ac.jp/naid/110008000705)</sup>. The project uses a [priority queue](https://en.wikipedia.org/wiki/Priority_queue) to store possible board states resulting from moves, and a [heuristic function](https://en.wikipedia.org/wiki/Heuristic_(computer_science)) that scores the states and optimizes the search for a solution.

<p  align="center">
<img src="https://i.imgur.com/AU7lCZW.gif" alt="Flow Free puzzle">
</p>

# The Problem

Boards are typically a square grid with any number of colors to connect. A _well-designed_ board (an assumption made by this solver) has a unique solution, and requires the entire board to be filled in without any "zig-zagging" paths. A consequence of being an NP-complete puzzle is that, although solutions can be verified quickly, there is no known efficient algorithm to find a solution, and the time required to solve the problem increases very quickly as the board size grows. How do we leverage a computer to quickly find the solution to a given board? We can devise a metric to score potential paths towards the solution, and we investigate the paths that maximize this function first.

<p  align="center">
<img src="https://i.imgur.com/f6xSXNk.png" alt="Flow Free puzzle" width="600">
</p>

Another barrier to quickly finding a solution to a Flow Free puzzle is that the number of options for the next move (extending a path by one cell in a given direction) is very high. This is called the [branching factor](https://en.wikipedia.org/wiki/Branching_factor) of the puzzle. For example, in a board with 8 color paths, the initial number of moves can be as high as `4*2*8=64`. At this rate, the number of possible states after only five moves can run over a billion. It quickly becomes apparent that we cannot solve puzzles using a brute-force approach. In combination with our heuristic function for ranking states to explore, we need to quickly **invalidate** states that cannot possibly lead to a solution. Spending a bit more time checking the validity of the state of a board will save exponentially more time by greatly reducing the set of moves we can make. Many thanks go to Matt Zucker's excellent write-up<sup>[[2]](https://mzucker.github.io/2016/08/28/flow-solver.html)</sup> of his solution to the Flow Free puzzle, from which I drew inspiration for many of the validity checks.

# Getting Started

This project is written in Scala and built using SBT. To get started with running the solver, clone the repository locally, then launch sbt.

```
$ git clone https://github.com/andy327/flow-solver.git
$ cd flow-solver/
$ sbt
```

From here you can `compile` the project, run unit tests using `test`, enter the `console` to interact with the library yourself, or `run` the solver on an input file.

# Representing the State of the Board

An initial puzzle configuration is represented by the `BoardDef` trait in the project. A puzzle is characterized by its size (number of rows and columns), as well as a set of endpoint locations for each color. Extending this trait is the `BoardState` trait, which represents the state of the board after a particular ordered sequence of 'moves' has been executed. The BoardState trait performs checks to determine whether it violates any properties of a valid state, which include checking for dead-ends, stranded paths, illegal empty regions, and the presence of 'chokepoints'. A BoardState is able to check if it has any 'forced' moves (where a path has no other option but to extend one cell in a particular direction).

In addition to BoardDef and BoardState, there is a `Solver` trait, a container for the priority queue and the definition of the heuristic function used to rank the states left to explore. The Solver continuously pops the next highest-scored BoardState from the queue, checks if it is a solution, and then scores and adds its next possible paths. The `StringParserBoard` trait provides a simple mechanism for creating a BoardDef from an ASCII representation of the initial board configuration.

```scala
object Board5x5 extends StringParserBoard with Solver {
  val board =
    """0----
      |-----
      |--1--
      |213-0
      |3---2""".stripMargin
}

Board5x5.solution().foreach(println) // solves the flow problem and prints the solution
```

The `FlowSolver` object provides a runnable method `def solveBoardFromFile(path: String)` for returning a solution given the path to an input file containing the ASCII encoding of a board.

# Issues and Future Development

Most of the speed gains I've made can be attributed to finding ways to mark a BoardState as invalid. I may try to introduce some more of these validity checks in the future. One of the more enjoyable aspects of developing this project has been opening the Flow Free game on my phone, loading up a puzzle, and trying to break down the choices I am (not always consciously) making as I find the solutions. On the other end, it has also been helpful to enable debugging in BoardStates, and analyze the series of states that are analyzed by the Solver, to look for dead-ends where the program spends too much time looking for further paths. While the Solver often approaches the solution quickly, particularly if there are a high number of forced initial moves, it will occasionally start off with a poor decision. When this happens, the Solver may generate thousands of states that will not lead to the unique solution. Currently, the method for generating the solution to a puzzle takes in arguments for a maximum number of iterations and retry attempts. By default, it will stop checking after the queue reads 5,000 states, and will retry solving the puzzle up to 10 times. This is a limitation I'd like to avoid making, and so some work needs to be put in to prevent the Solver from getting stuck in unsuccessful branches of the move tree.

# License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT)
