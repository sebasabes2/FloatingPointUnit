package floatingPointUnit

import chisel3._
import chisel3.util._

class FloatingPointUnit extends Module {
  val io = IO(new Bundle {
    val input1 = Input(UInt(32.W))
    val input2 = Input(UInt(32.W))
    val output = Output(UInt(32.W))
  })

  val flags = IO(new Bundle {
    val overflow = Output(Bool())
    val underflow = Output(Bool())
    val zero = Output(Bool())
    val inexact = Output(Bool())
    val nan = Output(Bool())
  })

  val exponentWidth = 8
  val mantissaWidth = 23
  val significandWidth = mantissaWidth + 1

  // Decode floating points
  val decoder1 = Module(new Decoder(exponentWidth, mantissaWidth))
  val decoder2 = Module(new Decoder(exponentWidth, mantissaWidth))
  decoder1.io.input := io.input1
  decoder2.io.input := io.input2

  // ExponentMatcher
  val exponentMatcher = Module(new ExponentMatcher(exponentWidth, significandWidth))
  exponentMatcher.io.input1 := decoder1.io.output
  exponentMatcher.io.input2 := decoder2.io.output

  // PreAdder
  val preAdder = Module(new PreAdder(exponentWidth, significandWidth))
  preAdder.io.larger := exponentMatcher.io.larger
  preAdder.io.smaller := exponentMatcher.io.smaller

  // Adder
  val adder = Module(new Adder(exponentWidth, significandWidth))
  adder.io.input1 := preAdder.io.input1
  adder.io.input2 := preAdder.io.input2
  adder.io.subtract := preAdder.io.subtract

  // Normalizer
  val normalizer = Module(new Normalizer(exponentWidth, significandWidth))
  normalizer.io.input := adder.io.output

  // Rounder
  val rounder = Module(new Rounder(exponentWidth, significandWidth))
  rounder.io.input := RegNext(normalizer.io.output)

  // Encode output
  val encoder = Module(new Encoder(exponentWidth, mantissaWidth))
  encoder.io.input := rounder.io.output
  io.output := encoder.io.output

  // Flags
  flags.overflow := RegNext(normalizer.io.overflow) || rounder.io.overflow
  flags.underflow := RegNext(normalizer.io.underflow)
  flags.zero := RegNext(normalizer.io.zero)
  flags.inexact := rounder.io.inexact
  flags.nan := RegNext(adder.io.nan)
}
