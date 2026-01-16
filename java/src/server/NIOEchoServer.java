package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOEchoServer {
    private static final int DEFAULT_PORT = 8888;
    private static final int BUFFER_SIZE = 1024;

    private int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private boolean running = false;

    public NIOEchoServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        selector = Selector.open();

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        System.out.println("NIO Echo Server da khoi dong tren port " + port);
        System.out.println("Su dung co che I/O Multiplexing (select/poll/epoll)");
        System.out.println("Cho ket noi tu client...\n");

        eventLoop();
    }

    private void eventLoop() {
        while (running) {
            try {
                int readyChannels = selector.select(1000);

                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (IOException e) {
                        System.err.println("Loi xu ly ket noi: " + e.getMessage());
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Loi trong event loop: " + e.getMessage());
                break;
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            SelectionKey clientKey =
                clientChannel.register(selector, SelectionKey.OP_READ);

            clientKey.attach(ByteBuffer.allocate(BUFFER_SIZE));

            System.out.println("Client da ket noi: " + clientChannel.getRemoteAddress());
            System.out.println(
                "Tong so ket noi dang xu ly: " + (selector.keys().size() - 1)
            );
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            System.out.println(
                "Client da ngat ket noi: " + clientChannel.getRemoteAddress()
            );
            key.cancel();
            clientChannel.close();
            return;
        }

        if (bytesRead > 0) {
            buffer.flip();

            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data);

            System.out.println(
                "Nhan tu client " + clientChannel.getRemoteAddress() + ": " + message.trim()
            );

            buffer.rewind();
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        if (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }

        if (!buffer.hasRemaining()) {
            buffer.clear();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public void stop() {
        running = false;
        try {
            if (selector != null) {
                selector.close();
            }
            if (serverChannel != null) {
                serverChannel.close();
            }
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

        NIOEchoServer server = new NIOEchoServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nDang dung server...");
            server.stop();
        }));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Khong the khoi dong server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
