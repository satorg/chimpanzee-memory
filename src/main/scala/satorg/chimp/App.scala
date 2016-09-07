package satorg.chimp

import org.scalajs.dom

import scala.scalajs.js.JSApp

object App extends JSApp {
  private val canvas = dom.document.getElementById("canvas").asInstanceOf[dom.html.Canvas]

  private val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private val (canvasWidth, canvasHeight) = (canvas.width, canvas.height)

  private final val minCellCount = 5

  private final val cellBorder = 2
  private final val cellBorderDual = cellBorder * 2

  private val cellSize = {
    val maxCellWidth = (canvasWidth / minCellCount) >> 1 << 1
    val maxCellHeight = (canvasHeight / minCellCount) >> 1 << 1
    math.min(maxCellWidth, maxCellHeight)
  }
  private val cellSizeHalf = cellSize / 2
  private val cellInnerSize = cellSize - cellBorderDual

  private val colCount = canvasWidth / cellSize
  private val rowCount = canvasHeight / cellSize

  private var level: Int = 9
  private var state: State = newState()

  private def newState() = State(colCount, rowCount, level)

  override def main(): Unit = {
    canvas.onclick = onClickCanvas _

    for (level <- 3 to 9) {
      dom.document.
        getElementById(s"level$level").
        asInstanceOf[dom.html.Button].
        onclick = onClickLevel(_: dom.MouseEvent, level)
    }

    ctx.font = s"bold ${cellSize}px sans-serif"
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"

    drawState()
  }

  private def drawState(): Unit = {
    ctx.clearRect(0, 0, canvasWidth, canvasHeight)
    ctx.fillStyle = if (state.isFailed) "Red" else "White"

    if (state.nextNumber == 1 || state.isFailed)
      for (((col, row), index) <- state.numberPositions.zipWithIndex) {
        val numStr = (index + state.nextNumber).toString
        val numX = col * cellSize + cellSizeHalf
        val numY = row * cellSize + cellSizeHalf

        ctx.fillText(numStr, numX, numY)
      }
    else
      for ((col, row) <- state.numberPositions) {
        ctx.fillRect(
          col * cellSize + cellBorder,
          row * cellSize + cellBorder,
          cellInnerSize,
          cellInnerSize)
      }
  }

  private def onClickCanvas(ev: dom.MouseEvent): Unit = {
    if (state.isFinished) {
      state = newState()
      drawState()
      return
    }

    val clickedPos = {
      val rect = canvas.getBoundingClientRect()
      val evX = (ev.clientX - rect.left).toInt
      val evY = (ev.clientY - rect.top).toInt
      (evX / cellSize, evY / cellSize)
    }

    if (state.numberPositions.head == clickedPos) {
      state = state.dropNumber()
      drawState()
    }
    else if (state.numberPositions.tail.contains(clickedPos)) {
      state = state.fail()
      drawState()
    }
  }

  private def onClickLevel(ev: dom.MouseEvent, newLevel: Int): Unit = {
    level = newLevel
    state = newState()
    drawState()
  }
}
