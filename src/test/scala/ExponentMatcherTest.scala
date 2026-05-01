import floatingPointUnit.ExponentMatcher
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

  "ExponentMatcher" should "select positive sign" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x0".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x800000".U)

      dut.io.larger.sign.expect("x0".U)
      dut.io.larger.exponent.expect("x80".U)
      dut.io.larger.significand.expect("x800000".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x800000".U)
    }
  }

  "ExponentMatcher" should "set smallest bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x69".U)
      dut.io.input2.significand.poke("x800000".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000001".U)

      dut.io.smaller.guard.expect("x0".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x0".U)
    }
  }

  "ExponentMatcher" should "set guard bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x68".U)
      dut.io.input2.significand.poke("x800000".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000000".U)

      dut.io.smaller.guard.expect("x1".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x0".U)
    }
  }

  "ExponentMatcher" should "set round bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x67".U)
      dut.io.input2.significand.poke("x800000".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000000".U)

      dut.io.smaller.guard.expect("x0".U)
      dut.io.smaller.round.expect("x1".U)
      dut.io.smaller.sticky.expect("x0".U)
    }
  }

  "ExponentMatcher" should "set sticky bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x66".U)
      dut.io.input2.significand.poke("x800000".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000000".U)

      dut.io.smaller.guard.expect("x0".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x1".U)
    }
  }

  "ExponentMatcher" should "set smallest bit and sticky bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x69".U)
      dut.io.input2.significand.poke("x800001".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000001".U)

      dut.io.smaller.guard.expect("x0".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x1".U)
    }
  }

  "ExponentMatcher" should "set guard bit and sticky bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x68".U)
      dut.io.input2.significand.poke("x800001".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000000".U)

      dut.io.smaller.guard.expect("x1".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x1".U)
    }
  }

  "ExponentMatcher" should "set round bit and sticky bit" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x67".U)
      dut.io.input2.significand.poke("x800001".U)

      dut.io.smaller.sign.expect("x1".U)
      dut.io.smaller.exponent.expect("x80".U)
      dut.io.smaller.significand.expect("x000000".U)

      dut.io.smaller.guard.expect("x0".U)
      dut.io.smaller.round.expect("x1".U)
      dut.io.smaller.sticky.expect("x1".U)
    }
  }

  "ExponentMatcher" should "pass Add-Shift-And-Special-Significands.fptest:1994" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x0".U)
      dut.io.input1.exponent.poke("x12".U)
      dut.io.input1.significand.poke("x880180".U)

      dut.io.input2.sign.poke("x0".U)
      dut.io.input2.exponent.poke("x28".U)
      dut.io.input2.significand.poke("xFFFFFF".U)

      dut.io.larger.sign.expect("x0".U)
      dut.io.larger.exponent.expect("x28".U)
      dut.io.larger.significand.expect("xFFFFFF".U)

      dut.io.smaller.sign.expect("x0".U)
      dut.io.smaller.exponent.expect("x28".U)
      dut.io.smaller.significand.expect("x000002".U)
      dut.io.smaller.guard.expect("x0".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x1".U)
    }
  }

  "ExponentMatcher" should "pass ieee754-test-suite/Add-Cancellation.fptest:20" in {
    test(new ExponentMatcher(8, 24)) { dut =>
      dut.io.input1.sign.poke("x0".U)
      dut.io.input1.exponent.poke("x28".U)
      dut.io.input1.significand.poke("xFFFFFF".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x29".U)
      dut.io.input2.significand.poke("x800000".U)

      dut.io.larger.sign.expect("x1".U)
      dut.io.larger.exponent.expect("x29".U)
      dut.io.larger.significand.expect("x800000".U)

      dut.io.smaller.sign.expect("x0".U)
      dut.io.smaller.exponent.expect("x29".U)
      dut.io.smaller.significand.expect("x7FFFFF".U)
      dut.io.smaller.guard.expect("x1".U)
      dut.io.smaller.round.expect("x0".U)
      dut.io.smaller.sticky.expect("x0".U)
    }
  }
}
