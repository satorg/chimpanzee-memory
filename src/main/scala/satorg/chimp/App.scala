package satorg.chimp

import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.util.Try

object App extends JSApp {
  private final val minLevel = 3
  private final val maxLevel = 9

  private final val minCellCount = 5

  private final val cellBorder = 2
  private final val cellBorderDual = cellBorder * 2

  private val canvas = dom.document.getElementById("canvas").asInstanceOf[dom.html.Canvas]
  private val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private val (canvasWidth, canvasHeight) = (canvas.width, canvas.height)

  private val cellSize = {
    val maxCellWidth = (canvasWidth / minCellCount) >> 1 << 1
    val maxCellHeight = (canvasHeight / minCellCount) >> 1 << 1
    math.min(maxCellWidth, maxCellHeight)
  }
  private val cellSizeHalf = cellSize / 2
  private val cellInnerSize = cellSize - cellBorderDual

  private val colCount = canvasWidth / cellSize
  private val rowCount = canvasHeight / cellSize

  private val viewWidth = colCount * cellSize
  private val viewHeight = rowCount * cellSize
  private val viewX = (canvasWidth - viewWidth) / 2
  private val viewY = (canvasHeight - viewHeight) / 2

  private var currentLevel: Int =
    Option(dom.window.localStorage.getItem("level")).
      flatMap { levelStr => Try {levelStr.toInt}.toOption }.
      filter { level => level >= minLevel && level <= maxLevel }.
      getOrElse(maxLevel)

  private var state: State = createState()

  private def createState() = State(colCount, rowCount, currentLevel)

  private def getButton(level: Int) = dom.document.getElementById(s"level$level").asInstanceOf[dom.html.Button]

  override def main(): Unit = {

    for (level <- minLevel to maxLevel) {
      val btn = getButton(level)

      btn.onclick = onClickLevel(_: dom.MouseEvent, level)
      if (level == currentLevel)
        btn.parentElement.className = "active"
    }

    canvas.onclick = onClickCanvas _

    ctx.font = s"bold ${cellSize}px sans-serif"
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"

    drawState()
  }

  private def drawState(): Unit = {
    ctx.clearRect(0, 0, canvasWidth, canvasHeight)
    ctx.fillStyle = if (state.isFailed) "Red" else "Beige"

    ctx.setTransform(1, 0, 0, 1, viewX, viewY)

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
      state = createState()
      drawState()
    }
    else {
      val rect = canvas.getBoundingClientRect()
      val evX = (ev.clientX - rect.left - viewX).toInt
      val evY = (ev.clientY - rect.top - viewY).toInt

      if (evX < 0 || evX >= viewWidth || evY < 0 || evY >= viewHeight)
        None
      else
        Some((evX / cellSize, evY / cellSize))
    }.foreach { clickedPos =>
      state.clickPosition(clickedPos).foreach { newState =>
        state = newState
        drawState()
      }
    }
  }

  private def onClickLevel(ev: dom.MouseEvent, newLevel: Int): Unit = {
    dom.window.localStorage.setItem("level", newLevel.toString)

    getButton(currentLevel).parentElement.className = ""
    getButton(newLevel).parentElement.className = "active"

    currentLevel = newLevel
    state = createState()
    drawState()
  }
}
