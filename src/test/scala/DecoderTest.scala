import floatingPointUnit.Decoder
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DecoderTest extends AnyFlatSpec with ChiselScalatestTester {
  "Decoder" should "pass Add-Shift-And-Special-Significands.fptest:1994" in {
    test(new Decoder(8, 23)) { dut =>
      // Input 1
      dut.io.input.poke("x09080180".U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("x12".U)
      dut.io.output.significand.expect("x880180".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)

      // Input2
      dut.io.input.poke("x147FFFFF".U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("x28".U)
      dut.io.output.significand.expect("xFFFFFF".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  }
  
  "Decoder" should "pass ieee754-test-suite/Add-Cancellation.fptest:20" in {
    test(new Decoder(8, 23)) { dut =>
      // Input 1
      dut.io.input.poke("x147FFFFF".U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("x28".U)
      dut.io.output.significand.expect("xFFFFFF".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)

      // Input2
      dut.io.input.poke("x94800000".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x29".U)
      dut.io.output.significand.expect("x800000".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  }
  
  "Decoder" should "pass ieee754-test-suite/Basic-Types-Intermediate.fptest:86" in {
    test(new Decoder(8, 23)) { dut =>
      // Input 1
      dut.io.input.poke("x7E9C0000".U)

      dut.io.output.sign.expect("x0".U)
      dut.io.output.exponent.expect("xFD".U)
      dut.io.output.significand.expect("x9C0000".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)

      // Input2
      dut.io.input.poke("xC0520D20".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("x80".U)
      dut.io.output.significand.expect("xD20D20".U)
      dut.io.output.guard.expect("x0".U)
      dut.io.output.round.expect("x0".U)
      dut.io.output.sticky.expect("x0".U)
      dut.io.output.infinity.expect(false.B)
      dut.io.output.nan.expect(false.B)
    }
  }
}
