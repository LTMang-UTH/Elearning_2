package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedEchoServer {
    private static final int DEFAULT_PORT = 8889;
    private static final int THREAD_POOL_SIZE = 100;

    private int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;

    public ThreadedEchoServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Khoi dong server
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("Threaded Echo Server da khoi dong tren port " + port);
        System.out.println("Su dung mo hinh da luong (Thread-per-Connection)");
        System.out.println("Thread pool size: " + THREAD_POOL_SIZE);
        System.out.println("Cho ket noi tu client...\n");

        // Vong lap chap nhan ket noi
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Loi chap nhan ket noi: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handler xu ly tung client trong thread rieng
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();
            System.out.println("Client da ket noi: " + clientAddress);
            System.out.println("Thread xu ly: " + Thread.currentThread().getName());

            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            ) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(
                        "Nhan tu client " + clientAddress + ": " + inputLine
                    );
                    out.println(inputLine);
                }

            } catch (IOException e) {
                System.err.println(
                    "Loi xu ly client " + clientAddress + ": " + e.getMessage()
                );
            } finally {
                try {
                    clientSocket.close();
                    System.out.println(
                        "Client da ngat ket noi: " + clientAddress
                    );
                } catch (IOException e) {
                    System.err.println("Loi dong socket: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Dung server
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("Server da dung.");
        } catch (IOException e) {
            System.err.println("Loi khi dung server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println(
                    "Port khong hop le, su dung port mac dinh: " + DEFAULT_PORT
                );
            }
        }

        ThreadedEchoServer server = new ThreadedEchoServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nDang dung server...");
            server.stop();
        }));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println(
                "Khong the khoi dong server: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
