import floatingPointUnit.LeadingOneDetector
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LeadingOneDetectorTest extends AnyFlatSpec with ChiselScalatestTester {
  "LeadingOneDetector" should "pass" in {
    test(new LeadingOneDetector(1)) { dut =>
      dut.io.input.poke("x1".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)

      dut.io.input.poke("x0".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(0.U)
    }

    test(new LeadingOneDetector(2)) { dut =>
      dut.io.input.poke("x3".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)

      dut.io.input.poke("x2".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)
      
      dut.io.input.poke("x1".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)
      
      dut.io.input.poke("x0".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(0.U)
    }

    test(new LeadingOneDetector(3)) { dut =>      
      dut.io.input.poke("x4".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(2.U)
      
      dut.io.input.poke("x2".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)
      
      dut.io.input.poke("x1".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)
      
      dut.io.input.poke("x0".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(0.U)
    }

    test(new LeadingOneDetector(4)) { dut =>
      dut.io.input.poke("x8".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(3.U)
      
      dut.io.input.poke("x4".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(2.U)
      
      dut.io.input.poke("x2".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(1.U)
      
      dut.io.input.poke("x1".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(0.U)
      
      dut.io.input.poke("x0".U)
      dut.io.zero.expect(true.B)
      dut.io.position.expect(0.U)
    }

    test(new LeadingOneDetector(24)) { dut =>
      dut.io.input.poke("x800000".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(23.U)

      dut.io.input.poke("x012345".U)
      dut.io.zero.expect(false.B)
      dut.io.position.expect(16.U)
    }
  }
}