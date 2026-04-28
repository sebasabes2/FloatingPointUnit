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

  var input1: Float = 3.14f
  var input2: Float = 3.14f
  var output: Float = 3.14f

  var file: String = ""
  var line: Integer = -1

  override def toString = {
    f"operation: $operation, rounding mode: $roundingMode, exceptions: $exceptions, input1: $input1, input2: $input2, output: $output"
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
    Source.fromFile(path).getLines.drop(4).map(getTestFromLine).toList
  }

  def getTestFromLine(line: String): TestData = {
    parseTestLine(line) match {
      case Some(testData) => return testData
      case None => throw new Exception(f"unable to parse test line: $line")
    }
  }

  def parseTestLine(line: String): Option[TestData] = {
    val parts = line.split(" ").iterator
    var next: String = parts.next
    var data = new TestData
    // Parse operation
    if (next == "b32+" || next == "b32-") {
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
    // Parse input 1
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
        next = parts.next;
      }
      case None => return None
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
    }
    return None
  }
}

class TestSuite extends AnyFlatSpec with ChiselScalatestTester {
  "FloatingPointUnit" should "pass test suite" in {
    test(new FloatingPointUnit) { dut =>
      val tests = TestParser.getAllTests("ieee754-test-suite")
    }
  }
}
