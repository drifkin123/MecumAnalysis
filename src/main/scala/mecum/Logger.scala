package mecum

import java.io.{BufferedWriter, File, FileWriter}
import java.util.Date
import java.text.SimpleDateFormat
import java.util.TimeZone

class Logger {
  private val file = new File(s"./logs/${currentDateTime()}.txt")
  private val bw = new BufferedWriter(new FileWriter(file))

  private def currentDateTime: () => String = () => {
    val tz: TimeZone = TimeZone.getTimeZone("UTC")
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    df.setTimeZone(tz)
    df.format(new Date())
  }

  def logToFile(content: String) = {
    bw.write(content)
  }

  def closeWriter() = {
    bw.close()
  }
}
