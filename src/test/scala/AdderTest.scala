import floatingPointUnit.Adder
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AdderTest extends AnyFlatSpec with ChiselScalatestTester {
  "Adder" should "add similar exponents" in {
    test(new Adder(8, 24)) { dut =>
      dut.io.input1.sign.poke("x1".U)
      dut.io.input1.exponent.poke("x80".U)
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("x800000".U)

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
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x7c".U)
      dut.io.input2.significand.poke("x800000".U)

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
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x68".U)
      dut.io.input2.significand.poke("xc00001".U)

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
      dut.io.input1.significand.poke("x800000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x1".U)
      dut.io.input2.significand.poke("x000001".U)

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
      dut.io.output.nan.expect(true.B)

      dut.io.input1.nan.poke(false.B)
      dut.io.input2.nan.poke(true.B)
      dut.io.output.nan.expect(true.B)

      dut.io.input1.nan.poke(true.B)
      dut.io.input2.nan.poke(true.B)
      dut.io.output.nan.expect(true.B)

      dut.io.input1.nan.poke(false.B)
      dut.io.input1.infinity.poke(true.B)
      dut.io.input1.sign.poke("x0".U)
      dut.io.input2.nan.poke(false.B)
      dut.io.input2.infinity.poke(true.B)
      dut.io.input2.sign.poke("x1".U)
      dut.io.output.nan.expect(true.B)
    }
  }
}
