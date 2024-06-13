import serial
import time
import numpy as np
import librosa
from tensorflow.keras.models import load_model
import socket

# 시리얼 포트 설정
SERIAL_PORT = 'COM3'  # 사용중인 시리얼 포트에 맞게 설정
BAUD_RATE = 115200
FILE_NAME = 'RECEIVED_RECORD.OGG'
PACKET_SIZE = 64

# 서버 설정
SERVER_IP = "192.168.0.6"  # 서버 IP 주소
SERVER_PORT = 12346       # 서버 포트 번호
MESSAGE = "1"             # 전송할 메시지

# 모델 불러오기
model = load_model("baby_cry_detection_model.h5")
print("RNN model loaded.")

def wait_for_serial_port(port, baud_rate):
    while True:
        try:
            ser = serial.Serial(port, baud_rate, timeout=1)
            return ser
        except serial.SerialException:
            print(f"포트 {port}를 인식하지 못했습니다. 다시 시도 중...")
            time.sleep(1)

def receive_file():
    try:
        with wait_for_serial_port(SERIAL_PORT, BAUD_RATE) as ser:
            # 준비 신호 수신
            while True:
                line = ser.readline().decode().strip()
                if line == "READY":
                    print("준비 신호 수신")
                    break
                time.sleep(1)
            
            # 시작 신호 전송
            ser.write(b"START\n")
            print("시작 신호 전송")

            # 파일 크기 수신
            file_size = 0
            while True:
                line = ser.readline().decode().strip()
                if line.startswith("SIZE:"):
                    file_size = int(line.split(":")[1])
                    print(f"파일 크기: {file_size} 바이트")
                    break

            # 파일 데이터 수신 및 저장
            with open(FILE_NAME, 'wb') as file:
                bytes_received = 0
                while bytes_received < file_size:
                    packet = ser.read(PACKET_SIZE)
                    file.write(packet)
                    bytes_received += len(packet)
                    print(f"{bytes_received}/{file_size} 바이트 수신 완료")
                    if bytes_received >= file_size:
                        print("수신 완료: 고지된 파일 크기 이상 수신")
                        break
            
            # 더 많은 데이터를 수신한 경우 파일을 저장하고 분류로 넘어가기
            if bytes_received >= file_size:
                return True

            # 종료 신호 수신
            while True:
                line = ser.readline().decode().strip()
                if line == "END":
                    print("파일 전송 완료 신호 수신")
                    break
        return True
    except serial.SerialException as e:
        print(f"Serial exception: {e}")
        print("Re-initializing connection...")
        return False

def extract_features(file_path, duration=5):
    try:
        audio, sample_rate = librosa.load(file_path, sr=None, duration=duration)
        mfccs = librosa.feature.mfcc(y=audio, sr=sample_rate, n_mfcc=13)
        mfccs_scaled = np.mean(mfccs.T, axis=0)
        return mfccs_scaled
    except Exception as e:
        print(f"Failed to extract features: {e}")
        return None

def preprocess_data(file_path):
    mfccs = extract_features(file_path)
    if mfccs is None:
        return None
    X = np.array([mfccs])
    X = np.expand_dims(X, axis=-1)  # Reshape for RNN input
    return X

def classify_audio(file_path):
    # Preprocess the new data
    X_new = preprocess_data(file_path)
    if X_new is None:
        return False

    # Predict using the loaded RNN model
    prediction = model.predict(X_new)

    # Post-process prediction (if binary classification)
    # Assuming binary classification: 0 = non-baby crying, 1 = baby crying
    is_baby_crying = (prediction > 0.5).astype(int)

    # Print the prediction
    print(f"File: {file_path}, Predicted label: {'Baby crying' if is_baby_crying else 'Not baby crying'}")
    return is_baby_crying

def send_signal_to_server(server_ip, server_port, message):
    try:
        # UDP 소켓 생성
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        
        # 서버로 메시지 전송
        udp_socket.sendto(message.encode(), (server_ip, server_port))
        print(f"Sent '{message}' to {server_ip}:{server_port}")
        
        # 소켓 닫기
        udp_socket.close()
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    while True:
        if receive_file():
            if classify_audio(FILE_NAME):
                send_signal_to_server(SERVER_IP, SERVER_PORT, MESSAGE)
        time.sleep(1)  # 잠시 대기 후 다시 시도
