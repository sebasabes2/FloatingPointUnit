package floatingPointUnit

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._

class FloatingPointUnit extends Module {
  val io = IO(new Bundle {
    val input1 = Input(UInt(32.W))
    val input2 = Input(UInt(32.W))
    val operation = Input(UInt(2.W))
    val roundingMode = Input(UInt(3.W))
    val output = Output(UInt(32.W))
    val flags = Output(new Bundle {
      val overflow = Bool()
      val underflow = Bool()
      val zero = Bool()
      val inexact = Bool()
      val nan = Bool()
    })
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
  exponentMatcher.io.subtraction := io.operation(0)

  // PreAdder
  val preAdder = Module(new PreAdder(exponentWidth, significandWidth))
  preAdder.io.larger := exponentMatcher.io.larger
  preAdder.io.smaller := exponentMatcher.io.smaller

  // Adder
  val adder = Module(new Adder(exponentWidth, significandWidth))
  adder.io.input1 := preAdder.io.input1
  adder.io.input2 := preAdder.io.input2
  adder.io.subtract := preAdder.io.subtract

  // Multiplier
  val multiplier = Module(new Multiplier(exponentWidth, significandWidth))
  multiplier.io.input1 := decoder1.io.output
  multiplier.io.input2 := decoder2.io.output

  // Combiner
  val combiner = Module(new Combiner(exponentWidth, significandWidth + 1, significandWidth * 2))
  combiner.io.addition := adder.io.output
  combiner.io.multiplication := multiplier.io.output
  combiner.io.operation := io.operation

  // Right normalizer
  val rightNormalizer = Module(new RightNormalizer(exponentWidth, significandWidth * 2 - 1))
  rightNormalizer.io.input := combiner.io.output

  // Left normalizer
  val leftNormalizer = Module(new LeftNormalizer(exponentWidth, significandWidth * 2 - 1))
  leftNormalizer.io.input := rightNormalizer.io.output

  // Shortener
  val shortener = Module(new Shortener(exponentWidth, significandWidth * 2 - 1, significandWidth))
  shortener.io.input := leftNormalizer.io.output

  // Rounder
  val rounder = Module(new Rounder(exponentWidth, significandWidth))
  rounder.io.input := shortener.io.output
  rounder.io.roundingMode := io.roundingMode

  // Renormalizer
  val renormalizer = Module(new RightNormalizer(exponentWidth, significandWidth))
  renormalizer.io.input := rounder.io.output

  // Encode output
  val encoder = Module(new Encoder(exponentWidth, mantissaWidth))
  encoder.io.input := renormalizer.io.output
  io.output := encoder.io.output
  io.flags := encoder.io.flags
}

object FloatingPointUnit extends App {
  emitVerilog(new FloatingPointUnit)
}

