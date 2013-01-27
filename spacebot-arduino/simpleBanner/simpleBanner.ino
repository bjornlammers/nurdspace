#define ROWS        7  // number of rows
#define SHREGISTERS 7  // number of shiftregisters, (56 ouputs, 'last' 6 are *not* visible/don't have leds connected.)

#define DATAPIN  9  // U1 pin 32 
#define LATCHPIN 10  // U1 pin 25 (storage register clock input, used as 'latch')
#define CLOCKPIN 11  // U1 pin 24

#define ROW0PIN  8  // U1 pin 39  
#define ROW1PIN  7  // U1 pin 38
#define ROW2PIN  6  // U1 pin 37
#define ROW3PIN  5  // U1 pin 36
#define ROW4PIN  4  // U1 pin 35
#define ROW5PIN  3  // U1 pin 34
#define ROW6PIN  2  // U1 pin 33

#include "spacefont.h"

const byte rowToPin[ROWS] = 
{ 
  ROW0PIN, ROW1PIN, ROW2PIN, ROW3PIN, ROW4PIN, ROW5PIN, ROW6PIN
};

static int broadcastCounter = 0;

  // Data is van links naar rechts, eerste zes bits niet zichtbaar
  static byte data[7][8] = {{B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111},
                            {B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111},
                            {B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111},
                            {B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111},
                            {B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111},
                            {B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111},
                            {B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111, B11111111}};
                            
static char buffer[128];
static int bufferPointer;
static int bufferEnd;
static int columnsFree = 0;

void setup() {
  bufferEnd = 0;
  bufferPointer = 0;
  
  Serial.begin(9600);
  Serial.println(SHREGISTERS);
  
  for(byte row = 0; row < ROWS; row++)      // row mosfets
  {
    pinMode(rowToPin[row], OUTPUT);      
    digitalWrite(rowToPin[row], HIGH);      // turn all /off/
  }

  pinMode(DATAPIN, OUTPUT);                 // shiftregister pins
  digitalWrite(DATAPIN, HIGH);              // default state
  pinMode(LATCHPIN, OUTPUT);
  digitalWrite(LATCHPIN, HIGH);       
  pinMode(CLOCKPIN, OUTPUT);
  digitalWrite(CLOCKPIN, LOW);        

  digitalWrite(LATCHPIN, LOW);              // 
  for(byte sr = 0; sr < SHREGISTERS; sr++)  // for all shiftregisters,
  {
    shiftOut(DATAPIN, CLOCKPIN, MSBFIRST, B11111111);  // send a byte of data, all *off* in this case.
  }
  digitalWrite(LATCHPIN, HIGH);  

}

void loop() {
  for(int i = 0; i < 10; i++) {
    for(byte row = 0; row < ROWS; row++) {
      digitalWrite(LATCHPIN, LOW);                   
      for(byte sr = 0; sr < SHREGISTERS; sr++) {
        shiftOut(DATAPIN, CLOCKPIN, MSBFIRST, data[(row + 6) % 7][sr]); // shift out a byte
      }
      digitalWrite(LATCHPIN, HIGH);                  
      
      digitalWrite(rowToPin[row], LOW);              // /on/ Show the pixels on this row
      delayMicroseconds(500);                                   // show
      digitalWrite(rowToPin[row], HIGH);             // /off/ 
    }
  }
  shift();
  if (Serial.available() > 0) {
    char incomingByte = Serial.read();
    insertIntoBuffer(incomingByte);
  }
  if (broadcastCounter-- < 0) {
    broadcastCounter = 40;
    Serial.println("LEDPANEL port");
  }
}

void shiftWithCarry() {
  // Data shift
  for (byte row = 0; row < ROWS; row++) {
    byte carry = 1;
    for (byte sr = 0; sr <= SHREGISTERS; sr++) {
      if (sr == 0) {
        carry = (data[row][sr] & 128) >> 7;
      }
      // shift
      data[row][sr] = data[row][sr] << 1;
      if (sr == SHREGISTERS) {
        data[row][sr] = data[row][sr] | carry;
      } else {
        data[row][sr] = data[row][sr] | ((data[row][sr + 1] & 128) >> 7);
      }
    }
  }
}

void shift() {
  // Data shift
  for (byte row = 0; row < ROWS; row++) {
    for (byte sr = 0; sr <= SHREGISTERS; sr++) {
      // shift
      data[row][sr] = data[row][sr] << 1;
      if (sr == SHREGISTERS) {
        data[row][sr] = data[row][sr] | 1;
      } else {
        data[row][sr] = data[row][sr] | ((data[row][sr + 1] & 128) >> 7);
      }
    }
  }
  if (++columnsFree > 7) {
    columnsFree -= 8;
    insertCharFromBuffer();
  }
}

void insertIntoBuffer(char character) {
  buffer[bufferEnd] = character;
  bufferEnd++;
  bufferEnd = bufferEnd % 128;
}

void insertCharFromBuffer() {
  if (bufferPointer != bufferEnd) {
    insertCharacter(buffer[bufferPointer]);
    bufferPointer = (bufferPointer + 1) % 128;
  }
}

void insertCharacter(char character) {
  insertCharacter(font[character]);
}

void insertCharacter(byte character[]) {
  for (int i = 0; i < 7 ; i++) {
    data[i][7] = character[i];
  }    
}



