package chisel3.formal

import java.io.PrintWriter

import org.scalatest.FlatSpec
import chisel3.RawModule

import chisel3.formal.tags.Formal


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
  private var counter = 0
  def verify(dutGen: () => RawModule): Unit = {
    it should s"work in instance $counter" taggedAs(Formal) in {
      FormalVerify(dutGen)
    }
    counter += 1
  }
}
