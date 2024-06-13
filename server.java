package hello;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 2;
    private static final int UDP_PORT = 12346;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            new Thread(new UdpListener()).start();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                OutputStream output = socket.getOutputStream();
                this.writer = new PrintWriter(output, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try (InputStream input = socket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                String text;
                while ((text = reader.readLine()) != null) {
                    System.out.println("Received: " + text);
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            if (writer != null) {
                writer.println(message);
                System.out.println("Sent to client: " + message);
            }
        }
    }

    static class UdpListener implements Runnable {
        public void run() {
            try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received UDP signal: " + received);
                    if (received.equals("1")) {
                        sendTcpSignal("1");
                    }
                }
            } catch (IOException ex) {
                System.out.println("UDP Listener exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void sendTcpSignal(String signal) {
            for (ClientHandler client : clients) {
                client.sendMessage(signal);
            }
        }
    }
}
