package floatingPointUnit

import chisel3._
import chisel3.util._

class FloatingPointUnit extends Module {
  val io = IO(new Bundle {
    val input1 = Input(UInt(32.W))
    val input2 = Input(UInt(32.W))
    val operation = Input(UInt(2.W))
    val roundingMode = Input(UInt(3.W))
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

  // Adder modules

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

  val addRightNormalizer = Module(new RightNormalizer(exponentWidth, significandWidth))
  addRightNormalizer.io.input := adder.io.output

  // Normalizer
  val normalizer = Module(new Normalizer(exponentWidth, significandWidth))
  normalizer.io.input := addRightNormalizer.io.output

  // Multiplier modules

  val multiplier = Module(new Multiplier(exponentWidth, significandWidth))
  multiplier.io.input1 := decoder1.io.output
  multiplier.io.input2 := decoder2.io.output

  val multRightNormalizer = Module(new RightNormalizer(exponentWidth, significandWidth * 2 - 1))
  multRightNormalizer.io.input := multiplier.io.output

  val multNormalizer = Module(new Normalizer(exponentWidth, significandWidth * 2 - 1))
  multNormalizer.io.input := multRightNormalizer.io.output

  val multShortener = Module(new Shortener(exponentWidth, significandWidth * 2 - 1, significandWidth))
  multShortener.io.input := multNormalizer.io.output

  // Combiner
  val combiner = Module(new Combiner(exponentWidth, significandWidth))
  combiner.io.addition := normalizer.io.output
  combiner.io.multiplication := multShortener.io.output
  combiner.io.operation := io.operation

  // Rounder
  val rounder = Module(new Rounder(exponentWidth, significandWidth))
  rounder.io.input := combiner.io.combined
  rounder.io.roundingMode := io.roundingMode

  // Renormalizer
  val renormalizer = Module(new RightNormalizer(exponentWidth, significandWidth))
  renormalizer.io.input := rounder.io.output

  // Encode output
  val encoder = Module(new Encoder(exponentWidth, mantissaWidth))
  encoder.io.input := renormalizer.io.output
  io.output := encoder.io.output

  // Flags
  flags.overflow := addRightNormalizer.io.overflow || multiplier.io.overflow || multRightNormalizer.io.overflow || renormalizer.io.overflow
  flags.underflow := normalizer.io.underflow || multiplier.io.underflow || multNormalizer.io.underflow
  flags.zero := normalizer.io.zero
  flags.inexact := rounder.io.inexact
  flags.nan := adder.io.nan
}
