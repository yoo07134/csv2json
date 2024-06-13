// This is a very beta demo of Ogg Vorbis recording. It works...
// Connect a button to digital 7 on the Arduino and use that to
// start and stop recording.

// A mic or line-in connection is required. See page 13 of the
// datasheet for wiring

// Don't forget to copy the v44k1q05.img patch to your micro SD
// card before running this example!


// include SPI, MP3 and SD libraries
#include <SPI.h>
#include <Adafruit_VS1053.h>
#include <SD.h>

// define the pins used
#define RESET 8      // VS1053 reset pin (output)
#define CS 6        // VS1053 chip select pin (output)
#define DCS 7        // VS1053 Data/command select pin (output)
#define CARDCS 9     // Card chip select pin
#define DREQ 2       // VS1053 Data request, ideally an Interrupt pin

Adafruit_VS1053_FilePlayer musicPlayer = Adafruit_VS1053_FilePlayer(RESET, CS, DCS, DREQ, CARDCS);

File recording;  // the file we will save our recording to
#define RECBUFFSIZE 128  // 64 or 128 bytes.
uint8_t recording_buffer[RECBUFFSIZE];
uint8_t isRecording = false;

void prepareRecording() {
  // load plugin from SD card! We'll use mono 8KHz, normal quality
  if (! musicPlayer.prepareRecordOgg("a.img")) {
    Serial.println("Couldn't load plugin!");
    while (1);
  }
}

void startRecording() {
  Serial.println("Begin recording");
  isRecording = true;

  // Check if the file exists already
  char filename[15];
  strcpy(filename, "RECORD00.OGG");
  SD.remove(filename);
  //  for (uint8_t i = 0; i < 100; i++) {
  //    filename[6] = '0' + i / 10;
  //    filename[7] = '0' + i % 10;
  //    // create if does not exist, do not open existing, write, sync after write
  //    if (! SD.exists(filename)) {
  //      break;
  //    }
  //  }
  Serial.print("Recording to "); Serial.println(filename);
  recording = SD.open(filename, FILE_WRITE);
  if (! recording) {
    Serial.println("Couldn't open file to record!");
    while (1);
  }
  musicPlayer.startRecordOgg(true); // use microphone (for linein, pass in 'false');
}

void stopRecording() {
  Serial.println("End recording");
  musicPlayer.stopRecordOgg();
  isRecording = false;
  // flush all the data!
  saveRecordedData(isRecording);
  // close it up
  recording.close();
  delay(1000);
}

void setup() {
  Serial.begin(115200);
  Serial.println("Adafruit VS1053 Ogg Recording Test");

  // initialise the music player
  if (!musicPlayer.begin()) {
    Serial.println("VS1053 not found");
    while (1);  // don't do anything more
  }
  musicPlayer.sineTest(0x44, 500);    // Make a tone to indicate VS1053 is working

  if (!SD.begin(CARDCS)) {
    Serial.println("SD failed, or not present");
    while (1);  // don't do anything more
  }
  Serial.println("SD OK!");

  // Set volume for left, right channels. lower numbers == louder volume!
  //musicPlayer.setVolume(10, 10);

  prepareRecording();
}

void loop() {
  if (Serial.available()) {
    char c = Serial.read();

    if (c == 'R' && !isRecording) {
      startRecording();
    }

    if (c == 'S' && isRecording) {
      stopRecording();
    }

    if (c == 'P') {
      musicPlayer.reset();
      musicPlayer.setVolume(10, 10);
      Serial.println("Playing");
      musicPlayer.playFullFile("RECORD00.OGG");
      prepareRecording();
      musicPlayer.setVolume(0, 0);
    }
  }

  if (isRecording) {
    saveRecordedData(isRecording);
  }
}

uint16_t saveRecordedData(boolean isrecord) {
  uint16_t written = 0;

  // read how many words are waiting for us
  uint16_t wordswaiting = musicPlayer.recordedWordsWaiting();

  // try to process 256 words (512 bytes) at a time, for best speed
  while (wordswaiting > 256) {
    //Serial.print("Waiting: "); Serial.println(wordswaiting);
    // for example 128 bytes x 4 loops = 512 bytes
    for (int x = 0; x < 512 / RECBUFFSIZE; x++) {
      // fill the buffer!
      for (uint16_t addr = 0; addr < RECBUFFSIZE; addr += 2) {
        uint16_t t = musicPlayer.recordedReadWord();
        //Serial.println(t, HEX);
        recording_buffer[addr] = t >> 8;
        recording_buffer[addr + 1] = t;
      }
      if (! recording.write(recording_buffer, RECBUFFSIZE)) {
        Serial.print("Couldn't write "); Serial.println(RECBUFFSIZE);
        while (1);
      }
    }
    // flush 512 bytes at a time
    recording.flush();
    written += 256;
    wordswaiting -= 256;
  }

  wordswaiting = musicPlayer.recordedWordsWaiting();
  if (!isrecord) {
    Serial.print(wordswaiting); Serial.println(" remaining");
    // wrapping up the recording!
    uint16_t addr = 0;
    for (int x = 0; x < wordswaiting - 1; x++) {
      // fill the buffer!
      uint16_t t = musicPlayer.recordedReadWord();
      recording_buffer[addr] = t >> 8;
      recording_buffer[addr + 1] = t;
      if (addr > RECBUFFSIZE) {
        if (! recording.write(recording_buffer, RECBUFFSIZE)) {
          Serial.println("Couldn't write!");
          while (1);
        }
        recording.flush();
        addr = 0;
      }
    }
    if (addr != 0) {
      if (!recording.write(recording_buffer, addr)) {
        Serial.println("Couldn't write!"); while (1);
      }
      written += addr;
    }
    musicPlayer.sciRead(VS1053_SCI_AICTRL3);
    if (! (musicPlayer.sciRead(VS1053_SCI_AICTRL3) & _BV(2))) {
      recording.write(musicPlayer.recordedReadWord() & 0xFF);
      written++;
    }
    recording.flush();
  }

  return written;
}
