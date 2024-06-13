#include <SPI.h>
#include <SD.h>

#define SD_CS_PIN 9  // SD 카드의 칩 셀렉트 핀
#define READY_SIGNAL "READY"
#define START_SIGNAL "START"
#define END_SIGNAL "END"
#define SIZE_PREFIX "SIZE:"
#define PACKET_SIZE 64

File file;

void setup() {
  Serial.begin(115200);  // 시리얼 통신 속도 설정
  while (!Serial) {
    ;  // 시리얼 포트가 연결될 때까지 대기
  }

  if (!SD.begin(SD_CS_PIN)) {
    Serial.println("SD 카드 초기화 실패!");
    return;
  }

  file = SD.open("RECORD01.OGG", FILE_READ);
  if (!file) {
    Serial.println("파일 열기 실패!");
    return;
  }

  Serial.println("파일 전송 준비 완료");
}

void loop() {
  Serial.println(READY_SIGNAL);  // 준비 신호 전송
  delay(1000);  // 1초 대기

  if (Serial.available() > 0) {
    String response = Serial.readStringUntil('\n');
    if (response == START_SIGNAL) {
      Serial.println("파일 전송 시작");

      long fileSize = file.size();
      Serial.print(SIZE_PREFIX);
      Serial.println(fileSize);  // 파일 크기 전송

      while (file.available()) {
        byte buffer[PACKET_SIZE];
        int bytesRead = file.read(buffer, sizeof(buffer));
        Serial.write(buffer, bytesRead);
      }

      file.close();
      Serial.println(END_SIGNAL);  // 파일 전송 완료 신호 전송
      while (true) {
        ;  // 무한 대기
      }
    }
  }
}
