package mecum

object App extends App {
  val carData = AppWiring.carData

  println("Starting timer...")
  val timer = new Timer()
  timer.startTimer()

  try {
    carData.getCarsByMake("AJS")
  } catch {
    case e: Exception => {
      AppWiring.logger.logToFile(s"EXCEPTION: ${e.getMessage}")
      AppWiring.logger.closeWriter()
      System.exit(-1)
    }
  } finally {
    AppWiring.logger.closeWriter()
  }

  println(s"Total time: ${timer.totalTime}")
}

class Timer {
  private var initTime: Long = 0

  def startTimer: () => Unit = () => {
    initTime = currentTime()
  }

  private def currentTime: () => Long = () => new java.util.Date().getTime

  def totalTime: () => String = () => {
    val secondsItTook = (new java.util.Date().getTime - initTime) / 1000.0
    val hours = (secondsItTook / 60 / 60).toInt
    val minutes = (secondsItTook / 60).toInt % 60
    val seconds = secondsItTook.toInt % 60

    s"${hours}:${minutes}:${seconds}"
  }

}
