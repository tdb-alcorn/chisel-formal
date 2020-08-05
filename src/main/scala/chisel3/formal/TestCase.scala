package chisel3.formal

import chisel3._
import chisel3.util.HasBlackBoxInline


class TestCase extends BlackBox with HasBlackBoxInline {
  val io = IO(new TestCaseIO)
  setInline("TestCase.sv",
    """module TestCase(
      |  output [31:0] testCase
      |);
      |  wire [31:0] test_case;
      |  assign testCase = test_case;
      |endmodule
      |""".stripMargin)
}

class TestCaseIO extends Bundle {
  val testCase = Output(UInt(32.W))
}