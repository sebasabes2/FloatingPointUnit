import FloatingPointUnit._
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
    }
  }
}
