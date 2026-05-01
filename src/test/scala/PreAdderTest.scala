import floatingPointUnit.PreAdder
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PreAdderTest extends AnyFlatSpec with ChiselScalatestTester {
  "Decoder" should "pass ieee754-test-suite/Add-Cancellation.fptest:20" in {
    test(new PreAdder(8, 24)) { dut =>
      dut.io.larger.sign.poke("x1".U)
      dut.io.larger.exponent.poke("x29".U)
      dut.io.larger.significand.poke("x800000".U)
      dut.io.larger.guard.poke("x0".U)
      dut.io.larger.round.poke("x0".U)
      dut.io.larger.sticky.poke("x0".U)

      dut.io.smaller.sign.poke("x0".U)
      dut.io.smaller.exponent.poke("x29".U)
      dut.io.smaller.significand.poke("x7FFFFF".U)
      dut.io.smaller.guard.poke("x1".U)
      dut.io.smaller.round.poke("x0".U)
      dut.io.smaller.sticky.poke("x0".U)

      dut.io.input1.sign.expect("x1".U)
      dut.io.input1.exponent.expect("x29".U)
      dut.io.input1.significand.expect("x4000000".U)

      dut.io.input2.sign.expect("x0".U)
      dut.io.input2.exponent.expect("x29".U)
      dut.io.input2.significand.expect("x4000003".U)
    }
  }
}
