import floatingPointUnit.Multiplier
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MultiplierTest extends AnyFlatSpec with ChiselScalatestTester {
  "Multiplier" should "pass ieee754-test-suite/Basic-Types-Intermediate.fptest:86" in {
    test(new Multiplier(8, 24)) { dut =>
      dut.io.input1.sign.poke("x0".U)
      dut.io.input1.exponent.poke("xFD".U)
      dut.io.input1.significand.poke("x9C0000".U)

      dut.io.input2.sign.poke("x1".U)
      dut.io.input2.exponent.poke("x80".U)
      dut.io.input2.significand.poke("xD20D20".U)

      dut.io.output.sign.expect("x1".U)
      dut.io.output.exponent.expect("xFE".U)
      dut.io.output.significand.expect("x7FFFFF800000".U)
    }
  }
}
