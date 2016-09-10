package satorg.chimp

import scala.collection.mutable
import scala.util.Random

class State private(val numberPositions: List[(Int, Int)], val nextNumber: Int, val isFailed: Boolean) {

  def isFinished = numberPositions.isEmpty || isFailed

  def clickPosition(pos: (Int, Int)): Option[State] = {
    assert(!isFinished, "`State` is finished")

    PartialFunction.condOpt(numberPositions) {
      case `pos` :: tailPositions =>
        copy(numberPositions = tailPositions, nextNumber = nextNumber + 1)
      case _ :: tailPositions if tailPositions.contains(pos) =>
        copy(isFailed = true)
    }
  }

  private def copy(numberPositions: List[(Int, Int)] = this.numberPositions,
                   nextNumber: Int = this.nextNumber,
                   isFailed: Boolean = this.isFailed) =
    new State(numberPositions, nextNumber, isFailed)
}

object State {
  private val random = new Random

  private def generateNumberPositions(colCount: Int, rowCount: Int, level: Int): List[(Int, Int)] = {
    val numbers = mutable.ListBuffer.empty[(Int, Int)]
    for (order <- 1 to level) {
      numbers +=
        Stream.
          continually {(random.nextInt(colCount), random.nextInt(rowCount))}.
          dropWhile {numbers.contains}.
          head
    }
    numbers.result()
  }

  def apply(colCount: Int, rowCount: Int, level: Int): State =
    new State(generateNumberPositions(colCount, rowCount, level), nextNumber = 1, isFailed = false)
}
