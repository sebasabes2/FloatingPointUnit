import floatingPointUnit.FloatingPointUnit
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File
import scala.io.Source

class TestData {
  var operation: String = ""
  var roundingMode: String = ""

  var inexact = false
  var underflow = false
  var overflow = false
  var divisionByZero = false
  var invalid = false

  def exceptions = {
    (if (inexact) "x" else "") +
    (if (underflow) "u" else "") +
    (if (overflow) "o" else "") +
    (if (divisionByZero) "z" else "") +
    (if (invalid) "i" else "")
  }

  var input1 = Float.NaN
  var input2 = Float.NaN
  var hasInput2 = false
  var input3 = Float.NaN
  var hasInput3 = false
  var output = Float.NaN

  var file: String = ""
  var line: Integer = -1

  override def toString = {
    f"operation: $operation, rounding mode: $roundingMode, exceptions: $exceptions, input1: $input1${if (hasInput2) f", input2: $input2" else ""}${if (hasInput3) f", input3: $input3" else ""}, output: $output"
  }
}

object TestParser {
  def getAllTests(testDir: String): List[TestData] = {
    getTestsFromFile(getTestFiles(testDir)(0)) // substitute with foreach
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
      getTestFromLine(path, lineIndex, line)
    }).toList
  }

  def getTestFromLine(path: String, lineIndex: Integer, line: String): TestData = {
    parseTestLine(path, lineIndex, line) match {
      case Some(testData) => return testData
      case None => throw new Exception(f"unable to parse test line: $line")
    }
  }

  def parseTestLine(path: String, lineIndex: Integer, line: String): Option[TestData] = {
    val parts = line.split(" ").iterator
    var next: String = parts.next
    var data = new TestData
    data.file = path
    data.line = lineIndex
    // Parse operation
    if (next == "b32+" || next == "b32-" || next == "b32*+") {
      data.operation = next
      next = parts.next;
    } else {
      return None
    }
    // Parse rounding mode
    if (next == "=0") {
      data.roundingMode = next
      next = parts.next;
    } else {
      return None
    }
    // Parse trapped exceptions
    if (parseExceptions(next, data)) {
      next = parts.next
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
    return Some(data)
  }

  def parseExceptions(input: String, data: TestData): Boolean = {
    var x, u, v, w, o, z, i = false
    if (input.contains("x")) { x = true }
    if (input.contains("u")) { u = true }
    if (input.contains("v")) { v = true }
    if (input.contains("w")) { w = true }
    if (input.contains("o")) { o = true }
    if (input.contains("z")) { z = true }
    if (input.contains("i")) { i = true }
    if (List(x, u, v, w, o, z, i).count(b => b) == input.length) {
      data.inexact = x
      data.underflow = u || v || w
      data.overflow = o
      data.divisionByZero = z
      data.invalid = i
      return true
    } else {
      return false
    }
  }

  def parseFloat(input: String): Option[Float] = {
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

class TestResult {
  var passed = false
  var failed = false
  var skipped = false
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
      if (result.failed && failed == 0) { firstFail = test }
      if (result.passed) { passed += 1 }
      if (result.failed) { failed += 1 }
      if (result.skipped) { skipped += 1 }
    }
    println(f"[info] Test suite result:")
    println(f"[info]   $passed/${tests.length} passed")
    println(f"[info]   $failed/${tests.length} failed")
    println(f"[info]   $skipped/${tests.length} skipped")
    if (failed != 0) {
      println(f"[${Console.RED}error${Console.RESET}] Test suite failed")
      println(f"[${Console.RED}error${Console.RESET}] First test to fail was ${firstFail.file}:${firstFail.line}")
      println(f"[${Console.RED}error${Console.RESET}] ${firstFail.toString}")
      runTest(dut, firstFail, silent=false)
    }
  }

  def runTest(dut: FloatingPointUnit, test: TestData, silent: Boolean = true): TestResult = {
    val input1 = ("x" + java.lang.Float.floatToIntBits(test.input1).toHexString).U
    val input2 = ("x" + java.lang.Float.floatToIntBits(test.input2).toHexString).U
    val expectedOutput = ("x" + java.lang.Float.floatToIntBits(test.output).toHexString).U
    dut.io.a.poke(input1)
    dut.io.b.poke(input2)
    dut.clock.step(6)
    if (!silent) {
      dut.io.res.expect(expectedOutput)
    }
    val output = dut.io.res.peek
    val result = new TestResult
    if (output.litValue.toInt == expectedOutput.litValue.toInt) {
      result.passed = true
    } else {
      result.failed = true
    }
    return result
  }
}

class TestSuite extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "pass test suite" in {
    test(new FloatingPointUnit) { dut =>
      val tests = TestParser.getAllTests("ieee754-test-suite")
      TestRunner.runTests(dut, tests)
    }
  }
}
