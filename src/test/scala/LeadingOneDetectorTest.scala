import floatingPointUnit.LeadingOneDetector
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LeadingOneDetectorTest extends AnyFlatSpec with ChiselScalatestTester {
  "LeadingOneDetector" should "pass" in {
    test(new LeadingOneDetector(1)) { dut =>
      dut.io.input.poke("b1".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)

      dut.io.input.poke("b0".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(0.U)
    }

    test(new LeadingOneDetector(2)) { dut =>
      dut.io.input.poke("b11".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)

      dut.io.input.poke("b10".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)
      
      dut.io.input.poke("b01".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)
      
      dut.io.input.poke("b00".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(1.U)
    }

    test(new LeadingOneDetector(3)) { dut =>      
      dut.io.input.poke("b100".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)
      
      dut.io.input.poke("b010".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)
      
      dut.io.input.poke("b001".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(2.U)
      
      dut.io.input.poke("b000".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(2.U)
    }

    test(new LeadingOneDetector(4)) { dut =>
      dut.io.input.poke("b1000".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)
      
      dut.io.input.poke("b0100".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)
      
      dut.io.input.poke("b0010".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(2.U)
      
      dut.io.input.poke("b0001".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(3.U)
      
      dut.io.input.poke("b0000".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(3.U)
    }

    test(new LeadingOneDetector(24)) { dut =>
      dut.io.input.poke("x800000".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)

      dut.io.input.poke("x012345".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(7.U)

      dut.io.input.poke("x000001".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(23.U)
    }
  }
}