package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Tool để test server với nhiều client đồng thời
 * 
 * Giúp so sánh hiệu năng giữa NIO Server và Threaded Server
 */
public class MultiClientTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_CLIENT_COUNT = 100;
    private static final int MESSAGES_PER_CLIENT = 10;

    private String host;
    private int port;
    private int clientCount;

    public MultiClientTest(String host, int port, int clientCount) {
        this.host = host;
        this.port = port;
        this.clientCount = clientCount;
    }

    /**
     * Chạy test với nhiều client đồng thời
     */
    public void runTest() {
        System.out.println("Bat dau test voi " + clientCount + " client dong thoi...");
        System.out.println("Moi client gui " + MESSAGES_PER_CLIENT + " tin nhan\n");

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch latch = new CountDownLatch(clientCount);
        List<Long> responseTimes = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Tạo và chạy các client
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    long clientStartTime = System.currentTimeMillis();
                    runClient(clientId);
                    long clientEndTime = System.currentTimeMillis();
                    responseTimes.add(clientEndTime - clientStartTime);
                    latch.countDown();
                } catch (Exception e) {
                    System.err.println("Client " + clientId + " loi: " + e.getMessage());
                    latch.countDown();
                }
            });
        }

        try {
            // Chờ tất cả client hoàn thành
            latch.await(60, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();

            // Thống kê
            long totalTime = endTime - startTime;
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

            System.out.println("\n=== KET QUA TEST ===");
            System.out.println("Tong so client: " + clientCount);
            System.out.println("Tong thoi gian: " + totalTime + " ms");
            System.out.println("Thoi gian trung binh moi client: " +
                String.format("%.2f", avgResponseTime) + " ms");
            System.out.println("Throughput: " +
                String.format("%.2f", (clientCount * MESSAGES_PER_CLIENT * 1000.0 / totalTime)) +
                " messages/second");

        } catch (InterruptedException e) {
            System.err.println("Test bi gian doan: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Chạy một client test
     */
    private void runClient(int clientId) throws IOException {
        try (
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        ) {
            // Gửi các tin nhắn test
            for (int i = 0; i < MESSAGES_PER_CLIENT; i++) {
                String message = "Client-" + clientId + "-Message-" + i;
                out.println(message);

                // Đọc phản hồi
                String response = in.readLine();
                if (response == null) {
                    break;
                }

                // Verify echo
                if (!response.equals(message)) {
                    System.err.println(
                        "Client " + clientId +
                        ": Phan hoi khong khop! Gui: " + message + ", Nhan: " + response
                    );
                }
            }
        }
    }

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        int clientCount = DEFAULT_CLIENT_COUNT;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println(
                    "Port khong hop le, su dung port mac dinh: " + DEFAULT_PORT
                );
            }
        }
        if (args.length >= 3) {
            try {
                clientCount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println(
                    "So client khong hop le, su dung mac dinh: " + DEFAULT_CLIENT_COUNT
                );
            }
        }

        MultiClientTest test = new MultiClientTest(host, port, clientCount);
        test.runTest();
    }
}
