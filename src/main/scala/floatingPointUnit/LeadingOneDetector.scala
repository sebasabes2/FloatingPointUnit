package floatingPointUnit

import chisel3._
import chisel3.util._
import scala.math.pow

class LeadingOneDetector(size: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(size.W))
    val position = Output(UInt(log2Up(size).W))
    val zero = Output(Bool())
  })

  if (size == 1) {
    io.position := 0.U
    io.zero := !io.input(0)
  } else if (size == 2) {
    io.position := !io.input(1)
    io.zero := !io.input(1) && !io.input(0)
  } else {
    val higherSize = pow(2, (log2Up(size) - 1)).intValue
    val lowerSize = size - higherSize

    val lower = Module(new LeadingOneDetector(lowerSize))
    val higher = Module(new LeadingOneDetector(higherSize))

    lower.io.input := io.input(lowerSize - 1, 0)
    higher.io.input := io.input(size - 1, lowerSize)

    io.position := higher.io.zero ## Mux(higher.io.zero, lower.io.position, higher.io.position)
    io.zero := lower.io.zero && higher.io.zero
  }
}
