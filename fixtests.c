#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <fenv.h>

enum Operation {
  ADD,
  SUB,
  MUL
};

struct Test {
  int operation;
  int roundMode;
  char *trappedExceptions; 
  float operand1;
  float operand2;
  float result;
  char *exceptions;
};

struct Node {
  char *string;
  struct Node *next;
};

int parseFloat(char *string, float *output) {
  if (strcmp(string, "S") == 0 ||
      strcmp(string, "Q") == 0 ||
      strcmp(string, "#") == 0) {
    *output = NAN;
    return 0;
  }
  char sign = string[0];
  if (strcmp(&string[1], "Zero") == 0) {
    if (sign == '+') {
      *output = 0.0;
    } else {
      *output = -0.0;
    }
    return 0;
  }
  if (strcmp(&string[1], "Inf") == 0) {
    if (sign == '+') {
      *output = INFINITY;
    } else {
      *output = -INFINITY;
    }
    return 0;
  }
  unsigned int leadingbit;
  unsigned int fraction;
  int exponent;
  if (sscanf(&string[1], "%d.%XP%d", &leadingbit, &fraction, &exponent) != 3) {
    return -1;
  }
  exponent += 127 + leadingbit - 1;
  unsigned int bits = (sign == '+' ? 0 : 0x80000000) + (exponent << 23) + fraction;
  *output = *((float *)(&bits));
  return 0;
}

int parseTest(char *string, struct Test *test) {
  // Set pointers to NULL
  test->trappedExceptions = NULL;
  test->exceptions = NULL;
  // Parse strings
  char buffer[8][32];
  int result = sscanf(string, "%s %s %s %s %s %s %s %s", buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], buffer[5], buffer[6], buffer[7]);
  // Parse operation
  if (strcmp(buffer[0], "b32+") == 0) {
    test->operation = ADD;
  }
  else if (strcmp(buffer[0], "b32-") == 0) {
    test->operation = SUB;
  }
  else if (strcmp(buffer[0], "b32*") == 0) {
    test->operation = MUL;
  }
  else {
    return -1;
  }
  // Parse rounding mode
  if (strcmp(buffer[1], ">") == 0) {
    test->roundMode = FE_UPWARD;
  }
  else if (strcmp(buffer[1], "<") == 0) {
    test->roundMode = FE_DOWNWARD;
  }
  else if (strcmp(buffer[1], "0") == 0) {
    test->roundMode = FE_TOWARDZERO;
  }
  else if (strcmp(buffer[1], "=0") == 0) {
    test->roundMode = FE_TONEAREST;
  }
  else if (strcmp(buffer[1], "=^") == 0) {
    // Unsuported in C
    // If a problem, try FE_TONEAREST and hope they match
    return -1;
  }
  else {
    return -1;
  }
  // Variable index
  int index = 2;
  // Parse trapped exceptions
  if (buffer[index][0] == 'x' ||
      buffer[index][0] == 'u' ||
      buffer[index][0] == 'v' ||
      buffer[index][0] == 'w' ||
      buffer[index][0] == 'o' ||
      buffer[index][0] == 'z' ||
      buffer[index][0] == 'i') {
    test->trappedExceptions = malloc(strlen(buffer[index]) + 1);
    strcpy(test->trappedExceptions, buffer[index]);
    index++;
  }
  // Parse operand 1
  if (index == result || parseFloat(buffer[index], &test->operand1) != 0) {
    return -1;
  }
  index++;
  // Parse operand 2
  if (index == result || parseFloat(buffer[index], &test->operand2) != 0) {
    return -1;
  }
  index++;
  // Parse '->'
  if (index == result || strcmp(buffer[index], "->") != 0) {
    return -1;
  }
  index++;
  // Parse result
  if (index == result || parseFloat(buffer[index], &test->result) != 0) {
    return -1;
  }
  index++;
  // Parse raised exceptions
  if (index != result && 
    (buffer[index][0] == 'x' ||
     buffer[index][0] == 'u' ||
     buffer[index][0] == 'v' ||
     buffer[index][0] == 'w' ||
     buffer[index][0] == 'o' ||
     buffer[index][0] == 'z' ||
     buffer[index][0] == 'i')) {
    test->exceptions = malloc(strlen(buffer[index]) + 1);
    strcpy(test->exceptions, buffer[index]);
    index++;
  }

  return 0;
}

float runTest(struct Test *test) {
  fesetround(test->roundMode);
  if (test->operation == ADD) {
    return test->operand1 + test->operand2;
  }
  if (test->operation == SUB) {
    return test->operand1 - test->operand2;
  }
  if (test->operation == MUL) {
    return test->operand1 * test->operand2;
  }
  return NAN;
}

int floatcmp(float a, float b) {
  if (isnan(a) && isnan(b)) {
    return 1;
  }
  return a == b;
}

void formatFloatingPoint(char *dest, float f) {
  if (isnan(f)) {
    sprintf(dest, "Q");
    return;
  }
  int bits = *(int *)(&f);
  char sign = (bits >> 31) ? '-' : '+';
  if (f == INFINITY || f == -INFINITY) {
    sprintf(dest, "%cInf", sign);
    return;
  }
  if (f == 0.0 || f == -0.0) {
    sprintf(dest, "%cZero", sign);
    return;
  }
  int exponent = ((bits >> 23) & 0xFF) - 127;
  int leadingbit = 1;
  if (exponent == -127) {
    leadingbit = 0;
    exponent = -126;
  }
  int fraction = bits & 0x7FFFFF;
  sprintf(dest, "%c%d.%06XP%d", sign, leadingbit, fraction, exponent);
}

void formatTest(char *dest, struct Test *test) {
  char *operation = NULL;
  if (test->operation == ADD) {
    operation = "b32+";
  }
  if (test->operation == SUB) {
    operation = "b32-";
  }
  if (test->operation == MUL) {
    operation = "b32*";
  }
  char *roundMode = NULL;
  if (test->roundMode == FE_UPWARD) {
    roundMode = ">";
  }
  if (test->roundMode == FE_DOWNWARD) {
    roundMode = "<";
  }
  if (test->roundMode == FE_TOWARDZERO) {
    roundMode = "0";
  }
  if (test->roundMode == FE_TONEAREST) {
    roundMode = "=0";
  }
  char trappedExceptions[16];
  trappedExceptions[0] = '\0';
  if (test->trappedExceptions != NULL) {
    sprintf(trappedExceptions, "%s ", test->trappedExceptions);
  }
  char operand1[32];
  formatFloatingPoint(operand1, test->operand1);
  char operand2[32];
  formatFloatingPoint(operand2, test->operand2);
  char result[32];
  formatFloatingPoint(result, test->result);
  char exceptions[16];
  exceptions[0] = '\0';
  if (test->exceptions != NULL) {
    sprintf(exceptions, "%s ", test->exceptions);
  }
  sprintf(dest, "%s %s %s%s %s -> %s %s\n", operation, roundMode, trappedExceptions, operand1, operand2, result, exceptions);
}

void freeTest(struct Test *test) {
  if (test->trappedExceptions != NULL) {
    free(test->trappedExceptions);
  }
  if (test->exceptions != NULL) {
    free(test->exceptions);
  }
}

static int parsed = 0;
static int error = 0;

char *fixTest(char *string) {
  struct Test test;
  if (parseTest(string, &test) != 0) {
    freeTest(&test);
    return string;
  }
  parsed++;

  float result = runTest(&test);

  if (floatcmp(test.result, result)) {
    freeTest(&test);
    return string;
  }
  error++;

  test.result = result;

  char buffer[128];
  formatTest(buffer, &test);

  char *newString = malloc(strlen(buffer) + 1);
  strcpy(newString, buffer);
  free(string);
  freeTest(&test);
  return newString;
}

void readFileError(char *path) {
  printf("error reading file %s\n", path);
  exit(1);
}

void fixTestFile(char *path) {
  int bufferLength = 1024;
  char buffer[bufferLength];
  char *header[4];
  struct Node *head = NULL;
  struct Node *tail = NULL;
  // Open file
  FILE *f = fopen(path, "r");
  if (f == NULL) {
    readFileError(path);
  }
  // Read first four lines
  for (int i = 0; i < 4; i++) {
    if (fgets(buffer, bufferLength, f) == 0) {
      readFileError(path);
    }
    header[i] = malloc(strlen(buffer) + 1);
    if (header[i] == NULL) {
      readFileError(path);
    }
    strcpy(header[i], buffer);
  }
  // Read all test vectors
  while (fgets(buffer, bufferLength, f) != NULL) {
    struct Node *new = malloc(sizeof(struct Node));
    if (new == NULL) {
      readFileError(path);
    }
    new->string = malloc(strlen(buffer) + 1);
    if (new->string == NULL) {
      readFileError(path);
    }
    new->next = NULL;
    strcpy(new->string, buffer);
    if (head == NULL) {
      head = new;
      tail = new;
    } else {
      tail->next = new;
      tail = new;
    }
  }
  // Close file
  if (fclose(f) != 0) {
    readFileError(path);
  }
  // Fix all tests
  tail = head;
  while (tail != NULL) {
    tail->string = fixTest(tail->string);
    tail = tail->next;
  }
  // Open file again in write mode
  f = fopen(path, "w");
  // Write first four lines
  for (int i = 0; i < 4; i++) {
    fputs(header[i], f);
  }
  // Write all test vectors
  tail = head;
  while (tail != NULL) {
    fputs(tail->string, f);
    tail = tail->next;
  }
  // Cleanup
  for (int i = 0; i < 4; i++) {
    free(header[i]);
  }
  tail = head;
  while (tail != NULL) {
    head = tail->next;
    free(tail->string);
    free(tail);
    tail = head;
  }
}

int main(int argc, char *argv[]) {
  if (argc < 2) {
    printf("please provide input file(s)\n");
    return 1;
  }

  for (int i = 1; i < argc; i++) {
    fixTestFile(argv[i]);
  }

  printf("number of parsed tests: %d\n", parsed);
  printf("number of nonsense tests: %d\n", error);

  return 0;
}