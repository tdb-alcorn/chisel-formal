package test

import chisel3._
import chisel3.formal._

class TestModule extends Module {
    val io = IO(new Bundle {
        val in = Input(Bool())
        val a = Output(Bool())
        val b = Output(Bool())
    })

    val aReg = RegInit(true.B)
    val bReg = RegInit(false.B)
    io.a := aReg
    io.b := bReg

    aReg :=  io.in
    bReg := !io.in
}

// This should fail.
class TestModuleFormal extends TestModule with Formal {
    assert(aReg === !bReg)
}
// This should not.
class TestModuleFormalAfterReset extends TestModule with FormalAfterReset {
    assert(aReg === !bReg)
}


class TestFormalSpec extends FormalSpec {
    behavior of "FormalAfterReset"
    it should "only enforce assertions after reset" in {
        FormalVerify(() => new TestModuleFormalAfterReset)
    }

    behavior of "Formal"
    it should "enforce assertions before reset" in {
        try {
            FormalVerify(() => new TestModuleFormal)
            assert(false)
        } catch {
            case e: java.lang.AssertionError => {}
        }
    }
}