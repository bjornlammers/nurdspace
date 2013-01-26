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

const byte rowToPin[ROWS] = 
{ 
  ROW0PIN, ROW1PIN, ROW2PIN, ROW3PIN, ROW4PIN, ROW5PIN, ROW6PIN
};

static byte letter_a[7] = {B11000001,
                           B10011100,
                           B10011100,
                           B10000000,
                           B10011100,
                           B10011100,
                           B10011100};
                           
static byte letter_b[7] = {B10000001,
                           B10011100,
                           B10011100,
                           B10000001,
                           B10011100,
                           B10011100,
                           B10000001};
                           
static byte letter_c[7] = {B11000001,
                           B10011100,
                           B10011111,
                           B10011111,
                           B10011111,
                           B10011100,
                           B11000001};
                           
static byte letter_d[7] = {B10000001,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10000001};
                           
static byte letter_e[7] = {B10000000,
                           B10011111,
                           B10011111,
                           B10000000,
                           B10011111,
                           B10011111,
                           B10000000};
                           
static byte letter_f[7] = {B10000000,
                           B10011111,
                           B10011111,
                           B10000000,
                           B10011111,
                           B10011111,
                           B10011111};
                           
static byte letter_g[7] = {B11000001,
                           B10011100,
                           B10011111,
                           B10011000,
                           B10011100,
                           B10011100,
                           B11000001};
                           
static byte letter_h[7] = {B10011100,
                           B10011100,
                           B10011100,
                           B10000000,
                           B10011100,
                           B10011100,
                           B10011100};
                           
static byte letter_i[7] = {B10000000,
                           B11100011,
                           B11100011,
                           B11100011,
                           B11100011,
                           B11100011,
                           B10000000};
                           
static byte letter_j[7] = {B10000000,
                           B11110001,
                           B11110001,
                           B11110001,
                           B11110001,
                           B10010001,
                           B11000011};
                           
static byte letter_k[7] = {B10011100,
                           B10011001,
                           B10010011,
                           B10000111,
                           B10010011,
                           B10011001,
                           B10011100};
                           
static byte letter_l[7] = {B10011111,
                           B10011111,
                           B10011111,
                           B10011111,
                           B10011111,
                           B10011111,
                           B10000000};
                           
static byte letter_m[7] = {B10001000,
                           B10010100,
                           B10010100,
                           B10010100,
                           B10011100,
                           B10011100,
                           B10011100};
                           
static byte letter_n[7] = {B10011100,
                           B10001100,
                           B10001100,
                           B10010100,
                           B10011000,
                           B10011000,
                           B10011100};
                           
static byte letter_o[7] = {B11000001,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B11000001};
                           
static byte letter_p[7] = {B10000001,
                           B10011100,
                           B10011100,
                           B10000001,
                           B10011111,
                           B10011111,
                           B10011111};
                           
static byte letter_q[7] = {B11000001,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011000,
                           B11000000};
                           
static byte letter_r[7] = {B10000001,
                           B10011100,
                           B10011100,
                           B10000001,
                           B10011100,
                           B10011100,
                           B10011100};
                           
static byte letter_s[7] = {B11000001,
                           B10011100,
                           B10011111,
                           B11000001,
                           B11111100,
                           B10011100,
                           B11000001};
                           
static byte letter_t[7] = {B10000000,
                           B11100011,
                           B11100011,
                           B11100011,
                           B11100011,
                           B11100011,
                           B11100011};
                           
static byte letter_u[7] = {B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B11000000};
                           
static byte letter_v[7] = {B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B10011100,
                           B11001001,
                           B11100011};
                           
static byte letter_w[7] = {B10011100,
                           B10011100,
                           B10011100,
                           B10010100,
                           B10010100,
                           B10010100,
                           B11001001};
                           
static byte letter_x[7] = {B10011100,
                           B10011100,
                           B11001000,
                           B11000001,
                           B11001001,
                           B10011100,
                           B10011100};
                           
static byte letter_y[7] = {B10011100,
                           B10011100,
                           B11001000,
                           B11000001,
                           B11100111,
                           B11100111,
                           B11100111};
                           
static byte letter_z[7] = {B10000000,
                           B11111100,
                           B11111001,
                           B11100011,
                           B11001111,
                           B10011111,
                           B10000000};
                           
static byte cijfer_1[7] = {B11100011,
                           B11000011,
                           B10100011,
                           B11100011,
                           B11100011,
                           B11100011,
                           B10000000};
                           
static byte cijfer_2[7] = {B11000001,
                           B10011100,
                           B11111001,
                           B11110011,
                           B11100111,
                           B11001111,
                           B10000000};
                           
static byte cijfer_3[7] = {B11000001,
                           B10011100,
                           B11111100,
                           B11100001,
                           B11111100,
                           B10011100,
                           B11000001};
                           
static byte cijfer_4[7] = {B11110001,
                           B11100001,
                           B11001001,
                           B10011001,
                           B10000000,
                           B11111001,
                           B11111001};
                           
static byte cijfer_5[7] = {B10000000,
                           B10011111,
                           B10000001,
                           B11111100,
                           B11111100,
                           B10011100,
                           B11000001};
                           
static byte cijfer_6[7] = {B11000001,
                           B10011100,
                           B10011111,
                           B10000001,
                           B10011100,
                           B10011100,
                           B10000001};
                           
static byte cijfer_7[7] = {B10000000,
                           B10011100,
                           B11111001,
                           B11100001,
                           B11100111,
                           B11001111,
                           B10011111};
                           
static byte cijfer_8[7] = {B11000001,
                           B10011100,
                           B10011100,
                           B11000001,
                           B10011100,
                           B10011100,
                           B11000001};
                           
static byte cijfer_9[7] = {B11000001,
                           B10011100,
                           B10011100,
                           B11000000,
                           B11111001,
                           B11110011,
                           B11100111};
                           
static byte cijfer_0[7] = {B11000001,
                           B10011100,
                           B10011000,
                           B10010100,
                           B10001100,
                           B10011100,
                           B11000001};
                           
static byte teken_excl[7] = {B11100011,
                             B11000001,
                             B11000001,
                             B11100011,
                             B11110111,
                             B11111111,
                             B11100011};
                           
static byte teken_ques[7] = {B11000001,
                             B10011100,
                             B11111100,
                             B11110001,
                             B11100111,
                             B11111111,
                             B11100111};
                           
static byte teken_dash[7] = {B11111111,
                             B11111111,
                             B11111111,
                             B11000001,
                             B11111111,
                             B11111111,
                             B11111111};
                           
static byte teken_spce[7] = {B11111111,
                             B11111111,
                             B11111111,
                             B11111111,
                             B11111111,
                             B11111111,
                             B11111111};
                           
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
    if ('A' == character) {
      insertCharacter(letter_a);
    } else if ('B' == character) {
      insertCharacter(letter_b);
    } else if ('C' == character) {
      insertCharacter(letter_c);
    } else if ('D' == character) {
      insertCharacter(letter_d);
    } else if ('E' == character) {
      insertCharacter(letter_e);
    } else if ('F' == character) {
      insertCharacter(letter_f);
    } else if ('G' == character) {
      insertCharacter(letter_g);
    } else if ('H' == character) {
      insertCharacter(letter_h);
    } else if ('I' == character) {
      insertCharacter(letter_i);
    } else if ('J' == character) {
      insertCharacter(letter_j);
    } else if ('K' == character) {
      insertCharacter(letter_k);
    } else if ('L' == character) {
      insertCharacter(letter_l);
    } else if ('M' == character) {
      insertCharacter(letter_m);
    } else if ('N' == character) {
      insertCharacter(letter_n);
    } else if ('O' == character) {
      insertCharacter(letter_o);
    } else if ('P' == character) {
      insertCharacter(letter_p);
    } else if ('Q' == character) {
      insertCharacter(letter_q);
    } else if ('R' == character) {
      insertCharacter(letter_r);
    } else if ('S' == character) {
      insertCharacter(letter_s);
    } else if ('T' == character) {
      insertCharacter(letter_t);
    } else if ('U' == character) {
      insertCharacter(letter_u);
    } else if ('V' == character) {
      insertCharacter(letter_v);
    } else if ('W' == character) {
      insertCharacter(letter_w);
    } else if ('X' == character) {
      insertCharacter(letter_x);
    } else if ('Y' == character) {
      insertCharacter(letter_y);
    } else if ('Z' == character) {
      insertCharacter(letter_z);
    } else if ('1' == character) {
      insertCharacter(cijfer_1);
    } else if ('2' == character) {
      insertCharacter(cijfer_2);
    } else if ('3' == character) {
      insertCharacter(cijfer_3);
    } else if ('4' == character) {
      insertCharacter(cijfer_4);
    } else if ('5' == character) {
      insertCharacter(cijfer_5);
    } else if ('6' == character) {
      insertCharacter(cijfer_6);
    } else if ('7' == character) {
      insertCharacter(cijfer_7);
    } else if ('8' == character) {
      insertCharacter(cijfer_8);
    } else if ('9' == character) {
      insertCharacter(cijfer_9);
    } else if ('0' == character) {
      insertCharacter(cijfer_0);
    } else if ('!' == character) {
      insertCharacter(teken_excl);
    } else if ('?' == character) {
      insertCharacter(teken_ques);
    } else if ('-' == character) {
      insertCharacter(teken_dash);
    } else {
      insertCharacter(teken_spce);
    }
}

void insertCharacter(byte character[]) {
  for (int i = 0; i < 7 ; i++) {
    data[i][7] = character[i];
  }    
}



