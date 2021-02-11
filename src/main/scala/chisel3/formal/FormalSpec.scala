package chisel3.formal

import java.io.PrintWriter

import org.scalatest.FlatSpec
import chisel3.RawModule

import chisel3.formal.tags.{Formal => FormalTag}


/**
 * How to use:
 *
 *   1. Make a class that extends FormalSpec
 *   2. Call `verify` on every instance you want to test
 *
 * E.g. this code runs Symbiyosys on the module `MyModule` parametrized to use
 * either 8-bit or 32-bit data.
 *
 * ```
 * class MyModuleFormalSpec extends FormalSpec {
 *   Seq(
 *     () => new MyModule(UInt(8.W)),
 *     () => new MyModule(UInt(32.W)),
 *   ).map(verify(_))
 * }
 * ```
 */
abstract class FormalSpec extends FlatSpec {
  val sbyLineNumberPattern = """.*:\s*\d+(?:\.\d+)?-(\d+)(?:\.\d+)?.*""".r
  val firrtlLineNumberPattern = """.*//\s*@\[(.*)]\s*""".r
  private var counter = 0
  def verify(dutGen: () => RawModule): Unit = {
    it should s"work in instance $counter" taggedAs(FormalTag) in {
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
    counter += 1
  }

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
