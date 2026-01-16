# So sánh TCP Echo Server: Thread-per-Connection vs Java NIO Selector

## 1. Mục tiêu

Dự án này được xây dựng nhằm **so sánh hiệu năng** giữa hai mô hình lập trình server phổ biến:

* **Thread-per-Connection (đa luồng truyền thống)**
* **Non-blocking I/O với Java NIO Selector**

Thông qua việc mô phỏng **nhiều client kết nối đồng thời**, dự án giúp làm rõ sự khác biệt về:

* Tổng thời gian xử lý
* Thời gian phản hồi trung bình
* Throughput (số message/giây)
* Khả năng mở rộng (scalability)

---

## 2. Các thành phần chính

### 2.1 ThreadedEchoServer

* Mỗi client được xử lý bởi **một thread riêng**
* Sử dụng `ServerSocket` và `Socket`
* I/O **blocking**
* Thread được quản lý bằng `ExecutorService`

**Đặc điểm:**

* Dễ hiểu, dễ cài đặt
* Tốn nhiều tài nguyên khi số client tăng cao
* Hiệu năng giảm mạnh khi có nhiều kết nối đồng thời

---

### 2.2 NIOEchoServer

* Sử dụng **Java NIO Selector** (Non-blocking I/O)
* Một thread có thể xử lý **nhiều SocketChannel**
* Áp dụng mô hình **event-driven**

**Đặc điểm:**

* Không tạo thread cho mỗi kết nối
* Tận dụng I/O multiplexing (`select / poll / epoll`)
* Hiệu năng cao với số lượng client lớn

---

### 2.3 MultiClientTest

* Tool mô phỏng **nhiều client đồng thời**
* Mỗi client gửi **10 message** tới server
* Đo lường:

  * Tổng thời gian test
  * Thời gian trung bình mỗi client
  * Throughput (message/second)

---

## 3. Kết quả thực nghiệm

### Test với 1000 client đồng thời

#### 🔹 NIO Server

```
Tong so client: 1000
Tong thoi gian: 1304 ms
Thoi gian trung binh moi client: 830.57 ms
Throughput: 7668.71 messages/second
```

#### 🔹 Threaded Server

```
Tong so client: 1000
Tong thoi gian: 2129 ms
Thoi gian trung binh moi client: 994.29 ms
Throughput: 4697.04 messages/second
```

---

## 4. So sánh trực quan

| Tiêu chí          | Thread-per-Connection | NIO Selector            |
| ----------------- | --------------------- | ----------------------- |
| Mô hình           | 1 thread / 1 client   | 1 thread / nhiều client |
| I/O               | Blocking              | Non-blocking            |
| Context switching | Nhiều                 | Rất ít                  |
| Khả năng mở rộng  | Thấp                  | Cao                     |
| Throughput        | Thấp hơn              | Cao hơn                 |
| Phù hợp           | Ứng dụng nhỏ          | Server thực tế          |

---

## 5. Phân tích nguyên nhân

### Threaded Server

* Mỗi client tạo một thread → tốn bộ nhớ stack
* Nhiều thread gây **context switching** liên tục
* Thread bị block khi chờ I/O

➡️ Hiệu năng giảm mạnh khi số client lớn

### NIO Server

* Một event loop xử lý nhiều kết nối
* Socket idle không tiêu tốn CPU
* Không block thread khi chờ I/O

➡️ Tối ưu tài nguyên, hiệu năng cao với tải lớn

---

## 6. Kết luận

* Mô hình **Thread-per-Connection** phù hợp cho:

  * Ứng dụng nhỏ
  * Ít kết nối đồng thời
  * Yêu cầu cài đặt đơn giản

* **Java NIO Selector** là lựa chọn tối ưu cho:

  * Server nhiều client (>1000)
  * Ứng dụng mạng thực tế
  * Hệ thống cần throughput cao và ổn định

> **Kết luận ngắn gọn:** Thread phù hợp bài toán nhỏ, NIO sinh ra cho server hiệu năng cao.

---

## 7. Hướng phát triển

* So sánh thêm với **AsynchronousChannel (AIO)**
* Đo CPU / RAM usage khi tải cao
* Tối ưu NIO với buffer reuse và write batching
