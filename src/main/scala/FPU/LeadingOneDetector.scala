package FPU

import chisel3._
import chisel3.util._
import scala.math.pow

class LeadingOneDetector(size: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(size.W))
    val position = Output(UInt(log2Up(size).W))
    val zero = Output(Bool())
  })

  if (size == 1) {
    io.position := 0.U
    io.zero := !io.in(0)
  } else if (size == 2) {
    io.position := io.in(1)
    io.zero := !io.in(1) && !io.in(0)
  } else {
    val lowerSize = pow(2, (log2Up(size) - 1)).intValue
    val higherSize = size - lowerSize

    val lower = Module(new LeadingOneDetector(lowerSize))
    val higher = Module(new LeadingOneDetector(higherSize))

    lower.io.in := io.in(lowerSize - 1, 0)
    higher.io.in := io.in(size - 1, lowerSize)

    io.position := !higher.io.zero ## Mux(higher.io.zero, lower.io.position, higher.io.position)
    io.zero := lower.io.zero && higher.io.zero
  }
}
