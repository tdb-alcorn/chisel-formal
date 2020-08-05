package chisel3.formal

import chisel3._
import chisel3.util.ShiftRegister

object Delay {
  def apply[T <: Data](wire: T, n: Int): T = {
    if (n == 0) {
      wire
    } else {
      ShiftRegister(wire, n)
    }
  }
}
