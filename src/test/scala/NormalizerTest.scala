import floatingPointUnit.Normalizer
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class NormalizerTest extends AnyFlatSpec with ChiselScalatestTester {
  // TODO: fix tests

  // "Normalizer" should "normalize right" in {
  //   test(new Normalizer(8, 24)) { dut =>
  //     dut.io.input.sign.poke("x1".U)
  //     dut.io.input.exponent.poke("x7f".U)
  //     dut.io.input.significand.poke("x1000000".U)

  //     dut.io.output.sign.expect("x1".U)
  //     dut.io.output.exponent.expect("x80".U)
  //     dut.io.output.significand.expect("x800000".U)
  //   }
  // }

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

  // "Normalizer" should "normalize to infinity" in {
  //   test(new Normalizer(8, 24)) { dut =>
  //     dut.io.input.sign.poke("x1".U)
  //     dut.io.input.exponent.poke("xFE".U)
  //     dut.io.input.significand.poke("x1000000".U)

  //     dut.io.output.infinity.expect(true.B)
  //   }
  // }

  // "Normalizer" should "pass Add-Shift-And-Special-Significands.fptest:1994" in {
  //   test(new Normalizer(8, 24)) { dut =>
  //     dut.io.input.sign.poke("x0".U)
  //     dut.io.input.exponent.poke("x28".U)
  //     dut.io.input.significand.poke("x1000001".U)
  //     dut.io.input.guard.poke("x0".U)
  //     dut.io.input.round.poke("x0".U)
  //     dut.io.input.sticky.poke("x1".U)

  //     dut.io.output.sign.expect("x0".U)
  //     dut.io.output.exponent.expect("x29".U)
  //     dut.io.output.significand.expect("x800000".U)
  //     dut.io.output.guard.expect("x1".U)
  //     dut.io.output.round.expect("x0".U)
  //     dut.io.output.sticky.expect("x1".U)
  //   }
  // }

  "Normalizer" should "pass ieee754-test-suite/Add-Cancellation.fptest:20" in {
    test(new Normalizer(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x29".U)
      dut.io.input.significand.poke("x000000".U)
      dut.io.input.guard.poke("x1".U)
      dut.io.input.round.poke("x0".U)
      dut.io.input.sticky.poke("x0".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x11".U)
      dut.io.output.significand.expect("x800000".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
    }
  }
}
