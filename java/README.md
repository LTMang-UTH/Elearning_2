# TCP Echo Server - Non-blocking I/O với Selector (I/O Multiplexing)

Ứng dụng mô phỏng và minh họa các kỹ thuật tối ưu hóa I/O bằng Java, tập trung vào **I/O Multiplexing** sử dụng Java NIO Selector (tương đương select/poll/epoll ở mức hệ điều hành).

## 📚 Mục tiêu học tập

- Hiểu sâu về cơ chế **I/O Multiplexing** ở mức hệ điều hành (select, poll, epoll)
- Biết cách sử dụng Java NIO Selector để xây dựng server non-blocking
- So sánh ưu nhược điểm của mô hình event-driven với đa luồng
- Hiểu cách các framework bất đồng bộ cấp cao được xây dựng

## 🏗️ Cấu trúc dự án

```
E2/
├── src/
│   ├── server/
│   │   ├── NIOEchoServer.java          # Server Non-blocking I/O với Selector
│   │   └── ThreadedEchoServer.java     # Server đa luồng (để so sánh)
│   └── client/
│       ├── EchoClient.java             # Client đơn giản để test
│       └── MultiClientTest.java        # Tool test với nhiều client đồng thời
├── pom.xml                              # Cấu hình Maven
├── run-nio-server.bat                   # Script chạy NIO Server
├── run-threaded-server.bat              # Script chạy Threaded Server
├── run-client.bat                       # Script chạy Client
├── run-test.bat                         # Script test với nhiều client
└── README.md                            # Tài liệu này
```

## 🔍 Kiến trúc

### 1. NIO Echo Server (Non-blocking I/O với Selector)

**File:** `src/server/NIOEchoServer.java`

- Sử dụng `java.nio.channels.Selector` - cơ chế I/O Multiplexing
- Một luồng xử lý nhiều kết nối (event-driven)
- Tận dụng epoll (Linux), kqueue (BSD/macOS), select (Windows)

**Cơ chế hoạt động:**

```
1. Selector theo dõi nhiều SocketChannel (file descriptors)
2. Khi có sự kiện I/O (đọc/ghi sẵn sàng), Selector thông báo
3. Server xử lý các sự kiện trên một luồng duy nhất
```

### 2. Threaded Echo Server (Đa luồng)

**File:** `src/server/ThreadedEchoServer.java`

- Mô hình Thread-per-Connection
- Mỗi client được xử lý bởi một thread riêng
- Sử dụng Thread Pool để quản lý

## 📊 So sánh các mô hình

| Tiêu chí                      | Thread-per-Connection | NIO Selector (Event-driven) |
| ----------------------------- | --------------------- | --------------------------- |
| **Số luồng**                  | 1 thread/client       | 1 thread cho nhiều client   |
| **Bộ nhớ**                    | ~1MB/thread           | ~1KB/connection             |
| **Context switching**         | Nhiều (tốn CPU)       | Ít                          |
| **Số kết nối tối đa**         | ~1000-5000            | ~10,000-100,000+            |
| **Độ phức tạp code**          | Đơn giản              | Phức tạp hơn                |
| **Hiệu năng (nhiều kết nối)** | Thấp                  | Cao                         |
| **Hiệu năng (ít kết nối)**    | Tốt                   | Tốt                         |
| **I/O Blocking**              | Có                    | Không                       |

## 🚀 HƯỚNG DẪN CHẠY VÀ DEMO

### Yêu cầu hệ thống

- **Java:** JDK 11 hoặc cao hơn
- **Maven:** 3.6+ (hoặc có thể chạy trực tiếp với Java)
- **Hệ điều hành:** Windows/Linux/macOS

### Bước 1: Compile dự án

Mở terminal/command prompt tại thư mục dự án và chạy:

```bash
mvn clean compile
```

### Bước 2: Chạy Server

#### Cách 1: Sử dụng Script (Windows)

**Chạy NIO Server (Non-blocking I/O):**

```bash
run-nio-server.bat
```

Server sẽ chạy trên port **8888**

**Chạy Threaded Server (Đa luồng):**

```bash
run-threaded-server.bat
```

Server sẽ chạy trên port **8889**

#### Cách 2: Sử dụng Maven

**Chạy NIO Server:**

```bash
mvn exec:java -Dexec.mainClass="server.NIOEchoServer" -Dexec.args="8888"
```

**Chạy Threaded Server:**

```bash
mvn exec:java -Dexec.mainClass="server.ThreadedEchoServer" -Dexec.args="8889"
```

#### Cách 3: Chạy trực tiếp với Java

Sau khi compile với `mvn clean package`:

```bash
# Chạy NIO Server
java -cp target/classes server.NIOEchoServer 8888

# Chạy Threaded Server
java -cp target/classes server.ThreadedEchoServer 8889
```

### Bước 3: Demo với Client

#### Demo 1: Client đơn giản (Interactive)

**Mở terminal mới** (giữ server đang chạy ở terminal cũ):

**Cách 1: Sử dụng Script**

```bash
run-client.bat
```

**Cách 2: Sử dụng Maven**

```bash
mvn exec:java -Dexec.mainClass="client.EchoClient" -Dexec.args="localhost 8888"
```

**Cách 3: Chạy trực tiếp**

```bash
java -cp target/classes client.EchoClient localhost 8888
```

**Sử dụng:**

- Nhập tin nhắn bất kỳ và nhấn Enter
- Server sẽ echo lại tin nhắn
- Gõ `quit` để thoát

**Ví dụ:**

```
Đã kết nối đến server localhost:8888
Nhập tin nhắn (hoặc 'quit' để thoát):

> Hello Server!
Server phản hồi: Hello Server!
> Test message
Server phản hồi: Test message
> quit
Đã ngắt kết nối.
```

#### Demo 2: Test với nhiều client đồng thời

**Mở terminal mới** (giữ server đang chạy):

**Cách 1: Sử dụng Script**

```bash
run-test.bat
```

Mặc định sẽ test với 100 client đồng thời

**Cách 2: Sử dụng Maven**

```bash
# Test với 100 client
mvn exec:java -Dexec.mainClass="client.MultiClientTest" -Dexec.args="localhost 8888 100"

# Test với 500 client
mvn exec:java -Dexec.mainClass="client.MultiClientTest" -Dexec.args="localhost 8888 500"
```

**Cách 3: Chạy trực tiếp**

```bash
java -cp target/classes client.MultiClientTest localhost 8888 100
```

**Kết quả sẽ hiển thị:**

```
=== KẾT QUẢ TEST ===
Tổng số client: 100
Tổng thời gian: 1234 ms
Thời gian trung bình mỗi client: 12.34 ms
Throughput: 810.37 messages/second
```

## 🧪 DEMO ĐẦY ĐỦ - So sánh NIO vs Threaded

### Demo Scenario: So sánh hiệu năng

#### Bước 1: Chạy NIO Server

**Terminal 1:**

```bash
mvn exec:java -Dexec.mainClass="server.NIOEchoServer" -Dexec.args="8888"
```

Bạn sẽ thấy:

```
NIO Echo Server đã khởi động trên port 8888
Sử dụng cơ chế I/O Multiplexing (select/poll/epoll)
Chờ kết nối từ client...
```

#### Bước 2: Test NIO Server với nhiều client

**Terminal 2:**

```bash
mvn exec:java -Dexec.mainClass="client.MultiClientTest" -Dexec.args="localhost 8888 200"
```

Ghi lại kết quả:

- Thời gian xử lý
- Throughput (messages/second)
- Số kết nối đồng thời

#### Bước 3: Dừng NIO Server và chạy Threaded Server

**Terminal 1:** Nhấn `Ctrl+C` để dừng NIO Server, sau đó chạy:

```bash
mvn exec:java -Dexec.mainClass="server.ThreadedEchoServer" -Dexec.args="8889"
```

#### Bước 4: Test Threaded Server với cùng số client

**Terminal 2:**

```bash
mvn exec:java -Dexec.mainClass="client.MultiClientTest" -Dexec.args="localhost 8889 200"
```

#### Bước 5: So sánh kết quả

So sánh:

- **Thời gian xử lý:** NIO Server thường nhanh hơn với nhiều kết nối
- **Throughput:** NIO Server có throughput cao hơn
- **Bộ nhớ:** NIO Server sử dụng ít bộ nhớ hơn (có thể kiểm tra bằng `jconsole`)

### Demo nâng cao: Monitor với nhiều kết nối

1. **Chạy NIO Server:**

   ```bash
   mvn exec:java -Dexec.mainClass="server.NIOEchoServer" -Dexec.args="8888"
   ```

2. **Mở nhiều terminal và chạy client:**

   ```bash
   # Terminal 2
   mvn exec:java -Dexec.mainClass="client.EchoClient" -Dexec.args="localhost 8888"

   # Terminal 3
   mvn exec:java -Dexec.mainClass="client.EchoClient" -Dexec.args="localhost 8888"

   # Terminal 4
   mvn exec:java -Dexec.mainClass="client.EchoClient" -Dexec.args="localhost 8888"
   ```

3. **Quan sát server log:**
   - Server sẽ hiển thị số kết nối đang xử lý
   - Tất cả được xử lý trên **1 luồng duy nhất**

## 📖 I/O Multiplexing - Giải thích chi tiết

### select(), poll(), epoll() là gì?

Đây là các system call ở mức kernel cho phép một process theo dõi nhiều file descriptor (socket) đồng thời.

#### select()

```c
int select(int nfds, fd_set *readfds, fd_set *writefds,
           fd_set *exceptfds, struct timeval *timeout);
```

- **Cách hoạt động:** Copy toàn bộ file descriptor set vào kernel, kernel kiểm tra và trả về
- **Nhược điểm:**
  - Giới hạn số lượng file descriptor (thường 1024)
  - Phải copy fd_set mỗi lần gọi (tốn bộ nhớ)
  - O(n) complexity khi kiểm tra

#### poll()

```c
int poll(struct pollfd *fds, nfds_t nfds, int timeout);
```

- **Cải tiến:** Không giới hạn số lượng file descriptor
- **Nhược điểm:** Vẫn phải duyệt qua tất cả file descriptors (O(n))

#### epoll() (Linux)

```c
int epoll_create1(int flags);
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);
int epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout);
```

- **Cải tiến lớn:**
  - Chỉ trả về file descriptors có sự kiện (O(1) với số lượng sự kiện)
  - Không cần copy file descriptor set mỗi lần
  - Hỗ trợ edge-triggered và level-triggered
  - Hiệu năng cao với nhiều kết nối

### Java NIO Selector

Java NIO Selector là abstraction layer trên các cơ chế này:

- **Linux:** Sử dụng epoll
- **macOS/BSD:** Sử dụng kqueue
- **Windows:** Sử dụng select (giới hạn)

```java
Selector selector = Selector.open(); // Tự động chọn cơ chế tốt nhất
serverChannel.register(selector, SelectionKey.OP_ACCEPT);
selector.select(); // Chờ sự kiện I/O
```

### Khi nào dùng mô hình nào?

**Thread-per-Connection phù hợp khi:**

- Ít kết nối đồng thời (< 100)
- Mỗi kết nối xử lý logic phức tạp, tốn thời gian
- Code đơn giản, dễ maintain

**NIO Selector phù hợp khi:**

- Nhiều kết nối đồng thời (> 1000)
- Kết nối có nhiều idle time (chat, monitoring, real-time)
- Cần hiệu năng cao với tài nguyên hạn chế
- I/O-bound operations (đọc/ghi network)

## 🔧 Troubleshooting

### Lỗi: Port đã được sử dụng

**Giải pháp:**

- Đổi port khác: `mvn exec:java -Dexec.mainClass="server.NIOEchoServer" -Dexec.args="9999"`
- Hoặc tìm và kill process đang dùng port:

  ```bash
  # Windows
  netstat -ano | findstr :8888
  taskkill /PID <PID> /F

  # Linux/macOS
  lsof -i :8888
  kill -9 <PID>
  ```

### Lỗi: ClassNotFoundException

**Giải pháp:**

- Đảm bảo đã compile: `mvn clean compile`
- Kiểm tra package name: `server.NIOEchoServer` (không phải `com.example.nio.server.NIOEchoServer`)

### Server không nhận kết nối

**Kiểm tra:**

- Firewall có chặn port không
- Server đã khởi động chưa
- Port number có đúng không

## 📚 Tài liệu tham khảo

### Java NIO

- [Java NIO Tutorial](https://docs.oracle.com/javase/tutorial/essential/io/index.html)
- [Java NIO Selector](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/Selector.html)

### System Calls

- `man select` (Linux)
- `man poll` (Linux)
- `man epoll` (Linux)

### Bài viết

- [The C10K Problem](http://www.kegel.com/c10k.html) - Vấn đề xử lý 10,000 kết nối đồng thời
- [Scalable Network Programming](https://www.usenix.org/legacy/event/usenix99/full_papers/banga/banga.pdf)

### Web Servers sử dụng Event-driven

- **Nginx:** Sử dụng epoll (Linux), kqueue (BSD)
- **Node.js:** Sử dụng libuv (epoll/kqueue/IOCP)
- **Netty (Java):** Framework NIO cho Java

## 🎯 Mở rộng

### Ý tưởng cải tiến:

1. **Thêm metrics:** Đo latency, throughput, số kết nối
2. **Protocol handler:** Xử lý HTTP thay vì echo đơn giản
3. **Connection pooling:** Quản lý kết nối tốt hơn
4. **SSL/TLS support:** Thêm bảo mật
5. **Load testing:** Sử dụng Apache Bench hoặc wrk
