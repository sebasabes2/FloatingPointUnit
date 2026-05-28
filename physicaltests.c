#include <stdio.h>
#include <fenv.h>

int asInt(float f) {
  return *((int *) &f);
}

void main() {
  float pi = 3.142857;
  float e = 2.71828;
  printf("Physical tests to be verified on fpuTester.FpuTester chisel module\n");
  printf("Tests include all operations and different rounding modes causing round up and round down\n");
  printf("operand1: %f, operand2: %f\n", pi, e);
  fesetround(FE_TONEAREST);
  float result = pi + e;
  printf("round nearest: 0x%08x + 0x%08x = 0x%08x (%.7f)\n", asInt(pi), asInt(e), asInt(result), result);
  fesetround(FE_DOWNWARD);
  result = pi + e;
  printf("round down:    0x%08x + 0x%08x = 0x%08x (%.7f)\n", asInt(pi), asInt(e), asInt(result), result);
  fesetround(FE_TONEAREST);
  result = pi - e;
  printf("round nearest: 0x%08x - 0x%08x = 0x%08x (%.7f)\n", asInt(pi), asInt(e), asInt(result), result);
  result = pi * e;
  printf("round nearest: 0x%08x * 0x%08x = 0x%08x (%.7f)\n", asInt(pi), asInt(e), asInt(result), result);
}
