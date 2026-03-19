import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class NormalizerTest extends AnyFlatSpec with ChiselScalatestTester {
  "Normalizer" should "normalize right" in {
    test(new Normalizer(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x7f".U)
      dut.io.input.significand.poke("x1000000".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800000".U)
    }
  }

  "Normalizer" should "not normalize" in {
    test(new Normalizer(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x0800000".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800000".U)
    }
  }

  "Normalizer" should "normalize left" in {
    test(new Normalizer(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x0080000".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x7c".U)
      dut.io.output.significand.expect("x800000".U)
    }
  }
}
