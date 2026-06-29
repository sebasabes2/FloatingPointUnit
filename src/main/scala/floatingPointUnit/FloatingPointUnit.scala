package floatingPointUnit

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._

class Stages {
  var input = false
  var output = false

  var exponentMatcher = false
  var adder = false
  var multiplier = false
  var selector = false
  var rightNormalizer = false
  var leftNormalizer = false
  var shortener = false
  var rounder = false
  var renormalizer = false
  var encoder = false
}

class FloatingPointUnit(exponentWidth: Int, fractionWidth: Int, stages: Stages = new Stages) extends Module {
  val floatingPointWidth = 1 + exponentWidth + fractionWidth
  val significandWidth = 1 + fractionWidth

  val io = IO(new Bundle {
    val input1 = Input(UInt(floatingPointWidth.W))
    val input2 = Input(UInt(floatingPointWidth.W))
    val operation = Input(UInt(2.W))
    val roundingMode = Input(UInt(3.W))
    val output = Output(UInt(floatingPointWidth.W))
    val flags = Output(new Bundle {
      val overflow = Bool()
      val underflow = Bool()
      val zero = Bool()
      val inexact = Bool()
      val nan = Bool()
    })
  })

  def chooseRegNext[T <: Data](choice: Boolean, input: => T): T = if (choice) RegNext(input) else input

  // Decode floating points
  val decoder1 = Module(new Decoder(exponentWidth, fractionWidth))
  val decoder2 = Module(new Decoder(exponentWidth, fractionWidth))
  decoder1.io.input := chooseRegNext(stages.input, io.input1)
  decoder2.io.input := chooseRegNext(stages.input, io.input2)
  val decoderStageOperation = chooseRegNext(stages.input, io.operation)
  val decoderStageRoundingMode = chooseRegNext(stages.input, io.roundingMode)

  // ExponentMatcher
  val exponentMatcher = Module(new ExponentMatcher(exponentWidth, significandWidth))
  exponentMatcher.io.input1 := chooseRegNext(stages.exponentMatcher, decoder1.io.output)
  exponentMatcher.io.input2 := chooseRegNext(stages.exponentMatcher, decoder2.io.output)
  exponentMatcher.io.subtraction := chooseRegNext(stages.exponentMatcher, decoderStageOperation(0))
  val exponentMatcherStageOperation = chooseRegNext(stages.exponentMatcher, decoderStageOperation)
  val exponentMatcherStageRoundingMode = chooseRegNext(stages.exponentMatcher, decoderStageRoundingMode)

  // Adder
  val adder = Module(new Adder(exponentWidth, significandWidth))
  adder.io.larger := chooseRegNext(stages.adder, exponentMatcher.io.larger)
  adder.io.smaller := chooseRegNext(stages.adder, exponentMatcher.io.smaller)
  val adderStageOperation = chooseRegNext(stages.adder, exponentMatcherStageOperation)
  val adderStageRoundingMode = chooseRegNext(stages.adder, exponentMatcherStageRoundingMode)

  // Multiplier
  val multiplier = Module(new Multiplier(exponentWidth, significandWidth))
  multiplier.io.input1 := chooseRegNext(stages.multiplier, decoder1.io.output)
  multiplier.io.input2 := chooseRegNext(stages.multiplier, decoder2.io.output)
  multiplier.io.roundingMode := chooseRegNext(stages.multiplier, decoderStageRoundingMode)

  // Selector
  val selector = Module(new Selector(exponentWidth, significandWidth + 1, significandWidth * 2))
  selector.io.addition := chooseRegNext(stages.selector, adder.io.output)
  selector.io.multiplication := chooseRegNext(stages.selector, multiplier.io.output)
  selector.io.operation := chooseRegNext(stages.selector, adderStageOperation)
  val selectorStageRoundingMode = chooseRegNext(stages.selector, adderStageRoundingMode)

  // Right normalizer
  val rightNormalizer = Module(new RightNormalizer(exponentWidth, significandWidth * 2 - 1))
  rightNormalizer.io.input := chooseRegNext(stages.rightNormalizer, selector.io.output)
  rightNormalizer.io.roundingMode := chooseRegNext(stages.rightNormalizer, selectorStageRoundingMode)
  val rightNormalizerStageRoundingMode = chooseRegNext(stages.rightNormalizer, selectorStageRoundingMode)

  // Left normalizer
  val leftNormalizer = Module(new LeftNormalizer(exponentWidth, significandWidth * 2 - 1))
  leftNormalizer.io.input := chooseRegNext(stages.leftNormalizer, rightNormalizer.io.output)
  val leftNormalizerStageRoundingMode = chooseRegNext(stages.leftNormalizer, rightNormalizerStageRoundingMode)

  // Shortener
  val shortener = Module(new Shortener(exponentWidth, significandWidth * 2 - 1, significandWidth))
  shortener.io.input := chooseRegNext(stages.shortener, leftNormalizer.io.output)
  val shortenerStageRoundingMode = chooseRegNext(stages.shortener, leftNormalizerStageRoundingMode)

  // Rounder
  val rounder = Module(new Rounder(exponentWidth, significandWidth))
  rounder.io.input := chooseRegNext(stages.rounder, shortener.io.output)
  rounder.io.roundingMode := chooseRegNext(stages.rounder, shortenerStageRoundingMode)
  val rounderStageRoundingMode = chooseRegNext(stages.rounder, shortenerStageRoundingMode)

  // Renormalizer
  val renormalizer = Module(new RightNormalizer(exponentWidth, significandWidth))
  renormalizer.io.input := chooseRegNext(stages.renormalizer, rounder.io.output)
  renormalizer.io.roundingMode := chooseRegNext(stages.renormalizer, rounderStageRoundingMode)

  // Encode output
  val encoder = Module(new Encoder(exponentWidth, fractionWidth))
  encoder.io.input := chooseRegNext(stages.encoder, renormalizer.io.output)

  // Output
  io.output := chooseRegNext(stages.output, encoder.io.output)
  io.flags := chooseRegNext(stages.output, encoder.io.flags)
}

class HalfPrecisionFloatingPointUnit(stages: Stages = new Stages) extends FloatingPointUnit(5, 10, stages)
class SinglePrecisionFloatingPointUnit(stages: Stages = new Stages) extends FloatingPointUnit(8, 23, stages)
class DoublePrecisionFloatingPointUnit(stages: Stages = new Stages) extends FloatingPointUnit(11, 52, stages)
