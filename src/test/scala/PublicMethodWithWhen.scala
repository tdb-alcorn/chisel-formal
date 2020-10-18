package test

import chisel3._
import chisel3.formal._
import chiseltest._
import org.scalatest._

/* This is a test case for a Module that uses FormalAfterReset and exposes public methods that use "when". */

class PublicMethodWithWhen extends FlatSpec with ChiselScalatestTester with Matchers {
    class ModuleWithMethod extends Module with FormalAfterReset {
        val io = IO(new Bundle {
            val in = Input(Bool())
        })
    
        def onTrue(block: => Any) = when(io.in)(block)
    }

    class ModuleThatUsesMethod extends Module {
        val io = IO(new Bundle {
            val in = Input(Bool())
            val out = Output(Bool())
        })

        val m = Module(new ModuleWithMethod())
        m.io.in := io.in

        io.out := false.B
        m.onTrue {
            io.out := true.B
        }
    }

    it should "not throw an exception" in {
        test(new ModuleThatUsesMethod()) { c=>
            c.io.in.poke(false.B)
            c.io.out.expect(false.B)

            c.io.in.poke(true.B)
            c.io.out.expect(true.B)
        }
    }
}
