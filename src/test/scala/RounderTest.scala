import FloatingPointUnit._
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RounderTest extends AnyFlatSpec with ChiselScalatestTester {
  "Rounder" should "not round" in {
    test(new Rounder(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x800000".U)
      dut.io.input.guard.poke("x0".U)
      dut.io.input.round.poke("x0".U)
      dut.io.input.sticky.poke("x1".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800000".U)
    }
  }

  "Rounder" should "round down" in {
    test(new Rounder(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x800000".U)
      dut.io.input.guard.poke("x0".U)
      dut.io.input.round.poke("x1".U)
      dut.io.input.sticky.poke("x1".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800000".U)
    }
  }

  "Rounder" should "round up" in {
    test(new Rounder(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x800000".U)
      dut.io.input.guard.poke("x1".U)
      dut.io.input.round.poke("x0".U)
      dut.io.input.sticky.poke("x1".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800001".U)
    }
  }

  "Rounder" should "tie down" in {
    test(new Rounder(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x800000".U)
      dut.io.input.guard.poke("x1".U)
      dut.io.input.round.poke("x0".U)
      dut.io.input.sticky.poke("x0".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800000".U)
    }
  }

  "Rounder" should "tie up" in {
    test(new Rounder(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("x800001".U)
      dut.io.input.guard.poke("x1".U)
      dut.io.input.round.poke("x0".U)
      dut.io.input.sticky.poke("x0".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800002".U)
    }
  }

  "Rounder" should "use extra bit" in {
    test(new Rounder(8, 24)) { dut =>
      dut.io.input.sign.poke("x1".U)
      dut.io.input.exponent.poke("x80".U)
      dut.io.input.significand.poke("xFFFFFF".U)
      dut.io.input.guard.poke("x1".U)
      dut.io.input.round.poke("x0".U)
      dut.io.input.sticky.poke("x0".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x1000000".U)
    }
  }
}
