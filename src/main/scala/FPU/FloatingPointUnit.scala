package FloatingPointUnit

import chisel3._
import chisel3.util._

class FloatingPointUnit extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val res = Output(UInt(32.W))
  })

  val exponentWidth = 8
  val mantissaWidth = 23
  val significandWidth = mantissaWidth + 1

  // Decode floating points
  val decoder1 = Module(new Decoder(exponentWidth, mantissaWidth))
  val decoder2 = Module(new Decoder(exponentWidth, mantissaWidth))
  decoder1.io.input := io.a
  decoder2.io.input := io.b

  // Adder
  val adder = Module(new Adder(exponentWidth, significandWidth))
  adder.io.input1 := decoder1.io.output
  adder.io.input2 := decoder2.io.output

  // Normalizer
  val normalizer = Module(new Normalizer(exponentWidth, significandWidth))
  normalizer.io.input := adder.io.output

  // Rounder
  val rounder = Module(new Rounder(exponentWidth, significandWidth))
  rounder.io.input := normalizer.io.output

  // Renormalizer
  val renormalizer = Module(new Normalizer(exponentWidth, significandWidth))
  renormalizer.io.input := rounder.io.output

  // Encode output
  val encoder = Module(new Encoder(exponentWidth, mantissaWidth))
  encoder.io.input := renormalizer.io.output
  io.res := encoder.io.output
}

object FloatingPointUnit extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new FloatingPointUnit)
}
