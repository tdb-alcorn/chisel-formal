package chisel3.formal

import chisel3._
import chisel3.util.HasBlackBoxInline


class ResetCounter extends BlackBox with HasBlackBoxInline {
  val io = IO(new ResetCounterIO)
  setInline("ResetCounter.sv",
    s"""`timescale 1ns/1ns
       |
       |module ResetCounter(
       |  input clock,
       |  input reset,
       |  output [31:0] numResets,
       |  output [31:0] timeSinceReset
       |);
       |  reg [31:0] num_resets;
       |  reg [31:0] t_since_reset;
       |  initial num_resets = 32'h00000000;
       |  initial t_since_reset = 32'h00000000;
       |  assign numResets = num_resets;
       |  assign timeSinceReset = t_since_reset;
       |
       |  always @(posedge clock) begin
       |    if (t_since_reset != 32'hFFFFFFFF) begin
       |      t_since_reset <= t_since_reset + 32'h00000001;
       |    end
       |    if (reset) begin
       |      if (num_resets != 32'hFFFFFFFF) begin
       |        num_resets <= num_resets + 32'h00000001;
       |      end
       |      t_since_reset <= 32'h00000000;
       |    end
       |  end
       |endmodule
       |""".stripMargin)
}

class ResetCounterIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Reset())
  val numResets = Output(UInt(32.W))
  val timeSinceReset = Output(UInt(32.W))
}
