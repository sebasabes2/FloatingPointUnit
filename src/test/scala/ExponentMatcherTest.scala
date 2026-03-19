import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ExponentMatcherTest extends AnyFlatSpec with ChiselScalatestTester {
  "ExponentMatcher" should "select largest significand" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x800002".U)

      dut.io.larger.sign.expect("x1".U)
      dut.io.larger.exponent.expect("x80".U)
      dut.io.larger.significand.expect("x800002".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x800000".U)
    }
  }

  "ExponentMatcher" should "select largest exponent" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x81".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x800002".U)

      dut.io.larger.sign.expect("x1".U)
      dut.io.larger.exponent.expect("x81".U)
      dut.io.larger.significand.expect("x800000".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x81".U)
      dut.io.smaller.significand.expect("x400001".U)
    }
  }
}
