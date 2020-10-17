package chisel3.formal

import java.io.PrintWriter
import chisel3.RawModule

object FormalVerify {
  def apply(dutGen: () => RawModule): Unit = {
    val result = Driver(dutGen)
    val rtlLines = result.rtl.linesIterator.toArray
    // write out symbiyosys output stream
    new PrintWriter(s"build/${result.moduleName}_sby_output.log") {
      write(result.output)
      close()
    }
    // write out symbiyosys error stream
    new PrintWriter(s"build/${result.moduleName}_sby_error.log") {
      write(result.error)
      close()
    }
    for (line <- result.output.linesIterator) {
      checkLine(result, rtlLines, line)
    }
  }

  val sbyLineNumberPattern = """.*:\s*\d+(?:\.\d+)?-(\d+)(?:\.\d+)?.*""".r
  val firrtlLineNumberPattern = """.*//\s*@\[(.*)]\s*""".r

  def checkLine(result: VerificationResult, rtlLines: Array[String],
                line: String): Unit = {
    val errorEncountered = line.toLowerCase.contains("error")
    val assertFailed = line.toLowerCase.contains("assert failed")
    val coverFailed = line.toLowerCase.contains("unreached cover statement")
    val message = if (coverFailed) {
      "Failed to reach cover statement at"
    } else if (assertFailed) {
      "Failed to assert condition at"
    } else {
      "Error encountered at"
    }
    if (assertFailed || coverFailed || errorEncountered) {
      line match {
        case sbyLineNumberPattern(rtlLineNumber) => {
          val rtlLine = rtlLines(rtlLineNumber.toInt-1)
          rtlLine match {
            case firrtlLineNumberPattern(scalaLine) => {
              assert(false, s"$message $scalaLine")
            }
            case _ => {
              assert(false, s"$message ${result.rtlFilename}" +
                s":$rtlLineNumber (no Scala line ref found): $rtlLine")
            }
          }
        }
        case _ => assert(false, s"$message $line")
      }
    }
  }
}