
// Intentionally different package to test public API
package chiselFormalTests

import chisel3._
import chisel3.formal.{Formal, FormalSpec}

class KeepMax(width: Int) extends Module with Formal {
  val io = IO(new Bundle {
    val in = Input(UInt(width.W))
    val out = Output(UInt(width.W))
  })

  val max = RegInit(0.U(width.W))
  when (io.in > max) {
    max := io.in
  }
  io.out := max

  // get the value of io.out from 1 cycle in the past
  past(io.out, 1) (pastIoOut => {
    assert(io.out >= pastIoOut)
  })
}

class SanitySpec extends FormalSpec {
  Seq(
    () => new KeepMax(1),
    () => new KeepMax(8),
  ).map(verify(_))
}
