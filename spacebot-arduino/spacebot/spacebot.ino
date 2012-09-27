#include <OneWire.h>

/* 
Multisensor readout for the Arduino that is connected to the SpaceBot server.

Reads:
-an LDR fixed to the fluorescent lighting of the space
-a DS18S20 Temperature chip

If more is connected, it would probably be better to use a timed mechanism. At 
this time I don't know how that works.

@author bjornl
*/

OneWire  ds(10);  // on pin 10

/*
Setup serial connection and assign read pin for LDR.
*/
void setup(void) {
  // initialize inputs/outputs
  // start serial port
  Serial.begin(9600);
  pinMode(A0, INPUT);
  ds.reset();
  pinMode(11, INPUT);
  digitalWrite(11, HIGH); // enable pull-up resistors
  pinMode(13, INPUT);
  digitalWrite(13, HIGH); // enable pull-up resistors
}

void loop(void) {
  readTemp();
  for (int i = 0; i < 30; i++) {
    readLDR();
    readLocks();
    delay(2000);
  }
}

void readLocks() {
  int backLockVal = digitalRead(11);
  Serial.print("Lock0: open: ");
  Serial.println(backLockVal);

  int frontLockVal = digitalRead(13);
  Serial.print("Lock1: open: ");
  Serial.println(frontLockVal);
}

void readLDR() {
  int val = analogRead(0);  
  Serial.print("LDR value: ");
  Serial.println(val);
}

void readTemp() {
  byte i;
  byte present = 0;
  byte data[12];
  byte addr[8];
  int Temp;
  int TempWhole;
  float TempFract;
  float TempFractFloat;
  float TempFloat;

  ds.reset_search();
  if ( !ds.search(addr)) {
        Serial.print("ERROR: No more addresses.\n");
        return;
  }

//  Serial.print("Search = ");  //R=28 Not sure what this is
//  for( i = 0; i < 8; i++) {
//    Serial.print(addr[i], HEX);
//  }
//  Serial.println();

  if ( OneWire::crc8( addr, 7) != addr[7]) {
        Serial.print("ERROR: CRC is not valid!\n");
        return;
  }

  if ( addr[0] != 0x28) {
        Serial.print("ERROR: Device is not a DS18S20 family device.\n");
        return;
  }

  ds.reset();
  ds.select(addr);
//  ds.write(0x44,1);      // start conversion, with parasite power on at the end
  ds.write(0x44);          // start conversion, with normal power

  delay(750);     // maybe 750ms is enough, maybe not
  // we might do a ds.depower() here, but the reset will take care of it.

  present = ds.reset();
  ds.select(addr);
  ds.write(0xBE);          // Read Scratchpad

//  Serial.print("Present = ");
//  Serial.println(present,HEX);
//  Serial.print("Data = ");
  for ( i = 0; i < 9; i++) {         // we need 9 bytes
    data[i] = ds.read();
//    Serial.print(data[i], HEX);
//    Serial.print(" ");
  }
//  Serial.println("");

  Temp=(data[1]<<8)+data[0];//take the two bytes from the response relating to temperature

  TempWhole=Temp>>4;//divide by 16 to get pure celcius readout
  TempFract=Temp & 0x0F;
  TempFractFloat = TempFract / 0x10;
  TempFloat = TempFractFloat + TempWhole;
  
//  Serial.print("TempFract: ");
//  Serial.println(TempFract, HEX);
//  Serial.print("TempFractFloat: ");
//  Serial.println(TempFractFloat);
  
  //next line is Fahrenheit conversion
  //Temp=Temp*1.8+32; // comment this line out to get celcius

  Serial.print("Temp: ");//output the temperature to serial port
  Serial.println(TempFloat);

//  Serial.print("CRC = ");
//  Serial.println( OneWire::crc8( data, 8), HEX);
}
