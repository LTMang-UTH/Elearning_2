package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client đơn giản để test Echo Server
 * 
 * Có thể kết nối đến cả NIO Server và Threaded Server
 */
public class EchoClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;

    private String host;
    private int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Kết nối và bắt đầu chat với server
     */
    public void start() {
        try (
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
        ) {
            System.out.println("Da ket noi den server " + host + ":" + port);
            System.out.println("Nhap tin nhan (hoac 'quit' de thoat):\n");

            // Thread đọc phản hồi từ server
            Thread readerThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("Server phan hoi: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Ket noi da dong.");
                }
            });
            readerThread.start();

            // Đọc input từ người dùng và gửi đến server
            String userInput;
            while (true) {
                System.out.print("> ");
                userInput = scanner.nextLine();

                if (userInput.equalsIgnoreCase("quit")) {
                    break;
                }

                // Gửi tin nhắn đến server
                out.println(userInput);
            }

            socket.close();
            System.out.println("Da ngat ket noi.");

        } catch (IOException e) {
            System.err.println("Loi ket noi den server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Port khong hop le, su dung port mac dinh: " + DEFAULT_PORT);
            }
        }

        EchoClient client = new EchoClient(host, port);
        client.start();
    }
}
