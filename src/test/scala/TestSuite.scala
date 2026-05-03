import floatingPointUnit.FloatingPointUnit
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File
import scala.io.Source

class Exceptions {
  var inexact = false
  var underflow = false
  var overflow = false
  var zero = false
  var nan = false

  override def toString = {
    (if (inexact) "x" else "") +
    (if (underflow) "u" else "") +
    (if (overflow) "o" else "") +
    (if (zero) "z" else "") +
    (if (nan) "i" else "")
  }
}

class TestData {
  var operation: String = ""
  var roundingMode: String = ""

  var trappedExceptions = new Exceptions

  var input1 = Float.NaN
  var input2 = Float.NaN
  var hasInput2 = false
  var input3 = Float.NaN
  var hasInput3 = false
  var output = Float.NaN

  var raisedExceptions = new Exceptions

  var line: String = ""
  var path: String = ""

  override def toString = {
    f"operation: $operation, rounding mode: $roundingMode, trapped exceptions: $trappedExceptions, input1: $input1${if (hasInput2) f", input2: $input2" else ""}${if (hasInput3) f", input3: $input3" else ""}, output: $output, raised exceptions: $raisedExceptions"
  }
}

object TestParser {
  def getAllTests(testDir: String): List[TestData] = {
    getTestFiles(testDir).map(getTestsFromFile).flatten
  }

  def getTestFiles(dir: String): List[String] = {
    new File(dir).listFiles
      .filter(_.isFile)
      .filter(_.getName.endsWith(".fptest"))
      .map(_.getPath).toList
  }

  def getTestsFromFile(path: String): List[TestData] = {
    var lineIndex = 4
    Source.fromFile(path).getLines.drop(4).map(line => {
      lineIndex += 1
      getTestFromLine(f"$path:$lineIndex", line)
    }).toList
  }

  def getTestFromLine(path: String, line: String): TestData = {
    parseTestLine(path, line) match {
      case Some(testData) => return testData
      case None => throw new Exception(f"unable to parse test line: ${"\""}$line${"\""} in $path")
    }
  }

  def parseTestLine(path: String, line: String): Option[TestData] = {
    var data = new TestData
    data.path = path
    data.line = line
    val parts = line.split(" ").iterator
    var next: String = parts.next
    // Parse operation
    if (List("b32+", "b32-", "b32*").contains(next)) {
      data.operation = next
      next = parts.next;
    } else if (next.slice(0,3) == "b32" || next.slice(0,3) == "d64" || next.slice(0,4) == "d128") {
      data.operation = next
      return Some(data) // Return because these operations will all be skipped so no need to parse them
    } else {
      return None
    }
    // Parse rounding mode
    if (List(">", "<", "0", "=0", "=^").contains(next)) {
      data.roundingMode = next
      next = parts.next;
    } else {
      return None
    }
    // Parse trapped exceptions
    parseExceptions(next) match {
      case Some(exceptions) => {
        data.trappedExceptions = exceptions
        next = parts.next;
      }
      case None =>
    }
    // Parse input 1. This input is mandatory
    parseFloat(next) match {
      case Some(float) => {
        data.input1 = float
        next = parts.next;
      }
      case None => return None
    }
    // Parse input 2
    parseFloat(next) match {
      case Some(float) => {
        data.input2 = float
        data.hasInput2 = true
        next = parts.next;
      }
      case None => 
    }
    // Parse input 3
    parseFloat(next) match {
      case Some(float) => {
        data.input3 = float
        data.hasInput3 = true
        next = parts.next;
      }
      case None => 
    }
    // Parse '->'
    if (next == "->") {
      next = parts.next
    } else {
      return None
    }
    // Parse output
    parseFloat(next) match {
      case Some(float) => data.output = float
      case None => return None
    }
    // Return if done parsing
    if (!parts.hasNext) {
      return Some(data)
    }
    next = parts.next
    // Parse raised exceptions
    parseExceptions(next) match {
      case Some(exceptions) => {
        data.raisedExceptions = exceptions
      }
      case None => return None
    }
    return Some(data)
  }

  def parseExceptions(input: String): Option[Exceptions] = {
    var x, u, v, w, o, z, i = false
    if (input.contains("x")) { x = true }
    if (input.contains("u")) { u = true }
    if (input.contains("v")) { v = true }
    if (input.contains("w")) { w = true }
    if (input.contains("o")) { o = true }
    if (input.contains("z")) { z = true }
    if (input.contains("i")) { i = true }
    if (List(x, u, v, w, o, z, i).count(b => b) == input.length) {
      var exceptions = new Exceptions
      exceptions.inexact = x
      exceptions.underflow = u || v || w
      exceptions.overflow = o
      exceptions.zero = z
      exceptions.nan = i
      return Some(exceptions)
    } else {
      return None
    }
  }

  def parseFloat(input: String): Option[Float] = {
    if (input == "S" || input == "Q" || input == "#") {
      return Some(Float.NaN)
    }
    val sign = input.slice(0, 1)
    if (sign != "+" && sign != "-") {
      return None
    }
    val rest = input.slice(1, input.length)
    if (rest.contains("P")) {
      val significand: String = rest.split("P")(0)
      val leading = significand.slice(0, 1)
      val mantissa = Integer.parseInt(significand.slice(2,significand.length), 16)
      val exponent = Integer.parseInt(rest.split("P")(1)) + 127 + (if (leading == "0") -1 else 0)
      val bits = (if (sign == "+") 0 else 0x80000000) + (exponent << 23) + mantissa
      val float = java.lang.Float.intBitsToFloat(bits) 
      return Some(float)
    } else {
      if (rest == "Zero") {
        return Some(if (sign == "+") 0.0f else -0.0f)
      }
      if (rest == "Inf") {
        return Some(if (sign == "+") Float.PositiveInfinity else Float.NegativeInfinity)
      }
    }
    return None
  }
}

object TestResult extends Enumeration {
  val passed, failed, skipped = Value
}

object TestRunner {
  def runTests(dut: FloatingPointUnit, tests: List[TestData]) {
    val iterator = tests.iterator
    var passed = 0
    var failed = 0
    var skipped = 0
    var firstFail = tests(0)
    while (iterator.hasNext) {
      val test = iterator.next
      val result = runTest(dut, test)
      if (result == TestResult.failed && failed == 0) { firstFail = test }
      if (result == TestResult.passed) { passed += 1 }
      if (result == TestResult.failed) { failed += 1 }
      if (result == TestResult.skipped) { skipped += 1 }
    }
    println(f"[info] Test suite result:")
    println(f"[info]   $passed passed")
    println(f"[info]   $failed failed")
    println(f"[info]   $skipped skipped")
    if (failed != 0) {
      println(f"[${Console.RED}error${Console.RESET}] Test suite failed")
      println(f"[${Console.RED}error${Console.RESET}] First test to fail was ${firstFail.path}")
      println(f"[${Console.RED}error${Console.RESET}] ${firstFail.toString}")
      println(f"[${Console.RED}error${Console.RESET}] ${firstFail.line}")
      runTest(dut, firstFail, silent=false)
    }
  }

  def runTest(dut: FloatingPointUnit, test: TestData, silent: Boolean = true): TestResult.Value = {
    // Select operation
    if (test.operation == "b32+") {
      dut.io.operation.poke(0.U)
    } else if (test.operation == "b32-") {
      dut.io.operation.poke(1.U)
    } else if (test.operation == "b32*") {
      dut.io.operation.poke(2.U)
    } else {
      return TestResult.skipped
    }
    // Select rounding mode
    if (test.roundingMode == "=0") {
      dut.io.roundingMode.poke(0.U)
    } else if (test.roundingMode == "=^") {
      dut.io.roundingMode.poke(1.U)
    } else if (test.roundingMode == ">") {
      dut.io.roundingMode.poke(2.U)
    } else if (test.roundingMode == "<") {
      dut.io.roundingMode.poke(3.U)
    } else if (test.roundingMode == "0") {
      dut.io.roundingMode.poke(4.U)
    } else {
      return TestResult.skipped
    }
    // Set inputs
    val input1 = ("x" + java.lang.Float.floatToIntBits(test.input1).toHexString).U
    val input2 = ("x" + java.lang.Float.floatToIntBits(test.input2).toHexString).U
    val expectedOutput = ("x" + java.lang.Float.floatToIntBits(test.output).toHexString).U
    dut.io.input1.poke(input1)
    dut.io.input2.poke(input2)
    // Check flags
    if (test.raisedExceptions.underflow) {
      if (!silent) { dut.flags.underflow.expect(true.B) }
      if (dut.flags.underflow.peek.litToBoolean) {
        return TestResult.passed
      } else {
        return TestResult.failed
      }
    }
    if (test.raisedExceptions.overflow) {
      if (!silent) { dut.flags.overflow.expect(true.B) }
      if (dut.flags.overflow.peek.litToBoolean) {
        return TestResult.passed
      } else {
        return TestResult.failed
      }
    }
    // Check result
    if (!silent) {
      dut.io.output.expect(expectedOutput)
    }
    val output = dut.io.output.peek
    if (output.litValue.toInt == expectedOutput.litValue.toInt) {
      return TestResult.passed
    } else {
      return TestResult.failed
    }
  }
}

class TestSuite extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "pass test suite" in {
    test(new FloatingPointUnit) { dut =>
      dut.clock.setTimeout(100000)
      val tests = TestParser.getAllTests("ieee754-test-suite")
      TestRunner.runTests(dut, tests)
    }
  }
}
