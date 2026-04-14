package floatingPointUnit

import chisel3._
import chisel3.util._
import scala.math.pow

class StickyBitDetector(size: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(size.W))
    val shift = Input(UInt(log2Up(size).W))
    val sticky = Output(Bool())
  })

  if (size == 2) {
    io.sticky := io.input(0) && io.shift(0) 
  } else {
    val lowerSize = pow(2, (log2Up(size) - 1)).intValue
    val higherSize = size - lowerSize

    val subDetector = Module(new StickyBitDetector(lowerSize))

    when (io.shift(log2Up(size) - 1)) {
      // When highest bit of shift is set
      // lowest half of input should be or'ed
      // remaining bits of shift should be tested on highest half
      subDetector.io.input := io.input(size - 1, lowerSize)
      subDetector.io.shift := io.shift(log2Up(size) - 2, 0)
      io.sticky := subDetector.io.sticky || io.input(lowerSize - 1,0).orR 
    } .otherwise {
      // When highest bit of shift is not set
      // highest half is ignored
      // remaining bits of shift should be tested on lowest half
      subDetector.io.input := io.input(lowerSize - 1, 0)
      subDetector.io.shift := io.shift(log2Up(size) - 2, 0)
      io.sticky := subDetector.io.sticky
    }
  }
}
