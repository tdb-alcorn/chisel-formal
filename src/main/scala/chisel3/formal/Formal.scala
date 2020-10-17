package chisel3.formal

import scala.reflect.ClassTag

import chisel3._
import chisel3.experimental.{verification => v}
import chisel3.internal.sourceinfo.SourceInfo
import chisel3.{when => chiselWhen}


trait Formal {
  this: Module =>

  private val resetCounter = Module(new ResetCounter)
  resetCounter.io.clock := clock
  resetCounter.io.reset := reset
  val numResets = resetCounter.io.numResets
  val timeSinceReset = resetCounter.io.timeSinceReset

  private val testCase = Module(new TestCase).io.testCase
  past(testCase, 1) (p => {
    assume(testCase === p)
  })

  def assert(predicate: Bool, message: String = "")
            (implicit sourceInfo: SourceInfo,
             compileOptions: CompileOptions): Unit = {
    v.assert(predicate, msg = message)
  }

  def assume(predicate: Bool, message: String = "")
            (implicit sourceInfo: SourceInfo,
             compileOptions: CompileOptions): Unit = {
    v.assume(predicate, msg = message)
  }

  def cover(predicate: Bool, message: String = "")
           (implicit sourceInfo: SourceInfo,
            compileOptions: CompileOptions): Unit = {
    v.cover(predicate, msg = message)
  }

  def when(predicate: Bool)(block: => Any)
          (implicit sourceInfo: SourceInfo,
           compileOptions: CompileOptions): WhenContext = {
    cover(predicate)
    chiselWhen(predicate)(block)
  }

  def past[T <: Data](value: T, n: Int)(block: T => Any)
                     (implicit sourceInfo: SourceInfo,
                      compileOptions: CompileOptions): Unit = {
    when (numResets >= 1.U && timeSinceReset >= n.U) {
      block(Delay(value, n))
    }
  }

  /**
   * history returns an array of past values
   *
   * history(0) is the current value
   * history(1) is the previous value == past(value, 1)
   * history(2) == past(value, 2)
   * ...
   *
   * @param value the Chisel value
   * @param n number of time steps to look back
   * @param block code using the returned history array
   */
  def history[T <: Data : ClassTag](value: T, n: Int)(block: Array[T] => Any)
                                   (implicit sourceInfo: SourceInfo,
                                    compileOptions: CompileOptions): Unit = {
    when (numResets >= 1.U && timeSinceReset > n.U) {
      val hist = for (i <- 0 until n + 1) yield Delay(value, i)
      block(hist.toArray)
    }
  }

  private var caseCounter: Int = 0
  def proofCase(n: Int = -1)(block: => Any)
               (implicit sourceInfo: SourceInfo,
                compileOptions: CompileOptions): Unit = {
    val caseNumber = if (n >= 0) {
      n
    } else {
      caseCounter
    }
    caseCounter += 1
    when (testCase === caseNumber.U) {
      block
    }
  }

  def afterReset(block: => Any)
                (implicit sourceInfo: SourceInfo,
                 compileOptions: CompileOptions): Unit = {
    chiselWhen(numResets >= 1.U)(block)
  }
}
