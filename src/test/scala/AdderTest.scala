import floatingPointUnit.Adder
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AdderTest extends AnyFlatSpec with ChiselScalatestTester {
  "Adder" should "add similar exponents" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x4000000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x4000000".U)

      dut.clock.step(1)

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
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x4000000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x400000".U)

      dut.clock.step(1)

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
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x4000000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x0000007".U)

      dut.clock.step(1)

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
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("xFE".U)
      dut.io.input1.significand.poke("x4000000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("xFE".U)
      dut.io.input2.significand.poke("x000001".U)

      dut.clock.step(1)

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
      dut.io.input1.nan.poke(true.B)
      dut.io.input2.nan.poke(false.B)
      dut.clock.step(1)
      dut.io.output.nan.expect(true.B)

      dut.io.input1.nan.poke(false.B)
      dut.io.input2.nan.poke(true.B)
      dut.clock.step(1)
      dut.io.output.nan.expect(true.B)

      dut.io.input1.nan.poke(true.B)
      dut.io.input2.nan.poke(true.B)
      dut.clock.step(1)
      dut.io.output.nan.expect(true.B)

      dut.io.input1.nan.poke(false.B)
      dut.io.input1.infinity.poke(true.B)
      dut.io.input1.sign.poke("x0".U)
      dut.io.input2.nan.poke(false.B)
      dut.io.input2.infinity.poke(true.B)
      dut.io.input2.sign.poke("x1".U)
      dut.clock.step(1)
      dut.io.output.nan.expect(true.B)
    }
  }

  "Adder" should "pass Add-Shift-And-Special-Significands.fptest:1994" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.input1.sign.poke("x0".U)
      dut.io.input1.exponent.poke("x28".U)
      dut.io.input1.significand.poke("x7FFFFF8".U)
      dut.io.input1.guard.poke("x0".U)
      dut.io.input1.round.poke("x0".U)
      dut.io.input1.sticky.poke("x0".U)

      dut.io.input2.sign.poke("x0".U)
      dut.io.input2.exponent.poke("x28".U)
      dut.io.input2.significand.poke("x0000011".U)
      dut.io.input2.guard.poke("x0".U)
      dut.io.input2.round.poke("x0".U)
      dut.io.input2.sticky.poke("x1".U)

      dut.io.subtract.poke(false.B)

      dut.clock.step(1)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("x28".U)
      dut.io.output.significand.expect("x1000001".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x1".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)

    }
  }
}
