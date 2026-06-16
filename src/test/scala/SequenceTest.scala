import floatingPointUnit.Stages
import floatingPointUnit.SinglePrecisionFloatingPointUnit
import floatingPointUnit.FloatingPointUnit
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class SequenceTestNumber {
  val input1: Float = Random.nextFloat()
  val input2: Float = Random.nextFloat()
  val operation: Int = Random.between(0, 3)
  val output: Float = operation match {
    case 0 => input1 + input2
    case 1 => input1 - input2
    case 2 => input1 * input2
  }
}

class SequenceTest extends AnyFlatSpec with ChiselScalatestTester {
  val numbersLength = 100
  val numbers = Range(0, numbersLength).toList.map(n => new SequenceTestNumber())

  def poke(dut: FloatingPointUnit, testNumber: SequenceTestNumber): Unit = {
    dut.io.input1.poke(("x" + java.lang.Float.floatToIntBits(testNumber.input1).toHexString).U)
    dut.io.input2.poke(("x" + java.lang.Float.floatToIntBits(testNumber.input2).toHexString).U)
    dut.io.operation.poke(testNumber.operation.U)
  }

  def expect(dut: FloatingPointUnit, testNumber: SequenceTestNumber): Unit = {
    dut.io.output.expect(("x" + java.lang.Float.floatToIntBits(testNumber.output).toHexString).U)
  }

  def testWithDelay(dut: FloatingPointUnit, delay: Int): Unit = {
    var i = 0;
    while (i - delay < numbersLength) {
      if (i < numbersLength) {
        poke(dut, numbers(i))
      }
      if (i - delay >= 0) {
        expect(dut, numbers(i - delay))
      }
      dut.clock.step(1)
      i += 1
    }
  }

  "FloatingPointUnit with no stages" should "pass" in {
    test(new SinglePrecisionFloatingPointUnit()) { dut =>
      testWithDelay(dut, 0)
    }
  }

  "FloatingPointUnit with input register" should "pass" in {
    test(new SinglePrecisionFloatingPointUnit(new Stages { input = true })) { dut =>
      testWithDelay(dut, 1)
    }
  }

  "FloatingPointUnit with output register" should "pass" in {
    test(new SinglePrecisionFloatingPointUnit(new Stages { output = true })) { dut =>
      testWithDelay(dut, 1)
    }
  }

  "FloatingPointUnit with input and output register" should "pass" in {
    test(new SinglePrecisionFloatingPointUnit(new Stages { input = true; output = true })) { dut =>
      testWithDelay(dut, 2)
    }
  }

  "FloatingPointUnit with three stages" should "pass" in {
    test(new SinglePrecisionFloatingPointUnit(new Stages { adder = true; multiplier = true; selector = true; shortener = true })) { dut =>
      testWithDelay(dut, 3)
    }
  }
}
