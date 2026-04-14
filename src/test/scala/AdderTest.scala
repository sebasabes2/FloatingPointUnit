import floatingPointUnit.Adder
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AdderTest extends AnyFlatSpec with ChiselScalatestTester {
  "Adder" should "add similar exponents" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.larger.sign.poke("x1".U)
      dut.io.larger.exponent.poke("x80".U)
      dut.io.larger.significand.poke("x800000".U)

      dut.io.smaller.sign.poke("x1".U)
      dut.io.smaller.exponent.poke("x80".U)
      dut.io.smaller.significand.poke("x800000".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x1000000".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  } 

  "Adder" should "add different exponents" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.larger.sign.poke("x1".U)
      dut.io.larger.exponent.poke("x80".U)
      dut.io.larger.significand.poke("x800000".U)

      dut.io.smaller.sign.poke("x1".U)
      dut.io.smaller.exponent.poke("x80".U)
      dut.io.smaller.significand.poke("x080000".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x880000".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  }

  "Adder" should "add resulting in round bits" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.larger.sign.poke("x1".U)
      dut.io.larger.exponent.poke("x80".U)
      dut.io.larger.significand.poke("x800000".U)

      dut.io.smaller.sign.poke("x1".U)
      dut.io.smaller.exponent.poke("x80".U)
      dut.io.smaller.significand.poke("x0000000".U)
      dut.io.smaller.guard.poke("x1".U)
      dut.io.smaller.round.poke("x1".U)
      dut.io.smaller.sticky.poke("x1".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("x800000".U)
      dut.io.output.guard.expect("x1".U)
      dut.io.output.round.expect("x1".U)
      dut.io.output.sticky.expect("x1".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  }

  "Adder" should "add resulting in sticky bit" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.larger.sign.poke("x1".U)
      dut.io.larger.exponent.poke("xFE".U)
      dut.io.larger.significand.poke("x800000".U)

      dut.io.smaller.sign.poke("x1".U)
      dut.io.smaller.exponent.poke("xFE".U)
      dut.io.smaller.significand.poke("x000000".U)
      dut.io.smaller.sticky.poke("x1".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("xFE".U)
      dut.io.output.significand.expect("x800000".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x1".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  }

  "Adder" should "add resulting in NaN" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.larger.nan.poke(true.B)
      dut.io.smaller.nan.poke(false.B)
      dut.io.output.nan.expect(true.B)

      dut.io.larger.nan.poke(false.B)
      dut.io.smaller.nan.poke(true.B)
      dut.io.output.nan.expect(true.B)

      dut.io.larger.nan.poke(true.B)
      dut.io.smaller.nan.poke(true.B)
      dut.io.output.nan.expect(true.B)

      dut.io.larger.nan.poke(false.B)
      dut.io.larger.infinity.poke(true.B)
      dut.io.larger.sign.poke("x0".U)
      dut.io.smaller.nan.poke(false.B)
      dut.io.smaller.infinity.poke(true.B)
      dut.io.smaller.sign.poke("x1".U)
      dut.io.output.nan.expect(true.B)
    }
  }
}
