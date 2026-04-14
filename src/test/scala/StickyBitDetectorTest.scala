import floatingPointUnit.StickyBitDetector
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class StickyBitDetectorTest extends AnyFlatSpec with ChiselScalatestTester {
  "LeadingOneDetector" should "pass" in {
    test(new StickyBitDetector(2)) { dut =>
      dut.io.input.poke("x3".U)
      dut.io.shift.poke("x0".U)
      dut.io.sticky.expect(false.B)

      dut.io.input.poke("x2".U)
      dut.io.shift.poke("x1".U)
      dut.io.sticky.expect(false.B)

      dut.io.input.poke("x3".U)
      dut.io.shift.poke("x1".U)
      dut.io.sticky.expect(true.B)
    }

    test(new StickyBitDetector(26)) { dut =>
      // When shift is zero no bits are considered
      dut.io.input.poke("x3ffffff".U)
      dut.io.shift.poke("x0".U)
      dut.io.sticky.expect(false.B)

      for (shift <- 1 until 26) {
        // All bits bellow shift number bit are considered
        for (i <- 1 until shift) {
          dut.io.input.poke((1 << i).U)
          dut.io.shift.poke(shift.U)
          dut.io.sticky.expect(true.B)
        }
        // All bits at or above shift number bit are not considered
        for (i <- shift until 26) {
          dut.io.input.poke((1 << i).U)
          dut.io.shift.poke(shift.U)
          dut.io.sticky.expect(false.B)
        }
      }
    }
  }
}