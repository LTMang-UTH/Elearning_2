# UDP Optimization Demo (Python)
## Mô tả

Ứng dụng mô phỏng các kỹ thuật tối ưu hóa giao thức UDP bằng Python, gồm 2 chế độ:
- **Basic**: UDP client thông thường
- **Optimized**: UDP client tối ưu hóa buffer gửi, gửi song song, đo RTT, loss, jitter
## File
- `udp_client.py`: UDP client, có 2 chế độ (Basic/Optimized)
- `udp_server.py`: UDP server (cần tương thích với client, nhận và trả lại struct `!Id`)

## Cách chạy

### 1. Chạy server
Mở terminal, chuyển vào thư mục Python:
```bash
cd Python
python udp_server.py
```

### 2. Chạy client
Mở terminal khác, chuyển vào thư mục Python:
```bash
cd Python
python udp_client.py
```
Chọn chế độ test:
- Nhập `1` để chạy chế độ Basic
- Nhập `2` để chạy chế độ Optimized

## Ý nghĩa các thông số
- **Loss rate**: Tỷ lệ mất gói tin
- **Avg RTT**: Thời gian khứ hồi trung bình
- **Min/Max RTT**: RTT nhỏ nhất/lớn nhất
- **Jitter**: Độ lệch chuẩn RTT (dao động độ trễ)

## Kỹ thuật tối ưu hóa UDP đã áp dụng
- Tăng buffer gửi (SO_SNDBUF)
- Gửi song song (threading)
- Giới hạn tốc độ gửi (rate limiting)
- Đo RTT, loss, jitter

## Yêu cầu
- Python 3.x

## Ghi chú
- Đảm bảo server và client dùng chung định dạng struct `!Id` (int, double)
- Có thể mở rộng thêm các kỹ thuật tối ưu khác nếu cần
# UDP Optimization Techniques - Các Kỹ Thuật Tối Ưu Hóa UDP

##  Giới Thiệu

Dự án này mô phỏng và demo các kỹ thuật tối ưu hóa giao thức UDP trong Python, bao gồm:

##  Các Kỹ Thuật Tối Ưu Hóa

### 1. **Buffer Size Optimization** (Tối ưu kích thước buffer)
- **Vấn đề**: Buffer mặc định có thể quá nhỏ, dẫn đến mất gói tin
- **Giải pháp**: Tăng kích thước buffer nhận (SO_RCVBUF) và gửi (SO_SNDBUF)
- **Code**:
```python
BUFFER_SIZE = 1024 * 1024  # 1MB
socket.setsockopt(socket.SOL_SOCKET, socket.SO_RCVBUF, BUFFER_SIZE)
socket.setsockopt(socket.SOL_SOCKET, socket.SO_SNDBUF, BUFFER_SIZE)
```
- **Lợi ích**: Giảm packet loss, tăng throughput

### 2. **Socket Options Tuning** (Tinh chỉnh tùy chọn socket)
- **SO_REUSEADDR**: Cho phép bind lại địa chỉ ngay sau khi đóng
- **SO_BROADCAST**: Cho phép gửi broadcast (nếu cần)
- **Code**:
```python
socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
```
- **Lợi ích**: Linh hoạt hơn trong việc quản lý socket

### 3. **Non-blocking I/O** (I/O không chặn)
- **Vấn đề**: Blocking I/O làm chậm xử lý
- **Giải pháp**: Sử dụng timeout hoặc non-blocking mode
- **Code**:
```python
socket.settimeout(0.1)  # 100ms timeout
# hoặc
socket.setblocking(False)
```
- **Lợi ích**: Server có thể xử lý nhiều request đồng thời

### 4. **Packet Batching** (Gộp gói tin)
- **Vấn đề**: Xử lý từng gói tin một rất kém hiệu quả
- **Giải pháp**: Nhận và xử lý nhiều gói tin cùng lúc
- **Code**:
```python
def receive_batch(self, max_packets=10):
    packets = []
    while len(packets) < max_packets:
        try:
            data, addr = self.socket.recvfrom(65535)
            packets.append((data, addr))
        except socket.timeout:
            break
    return packets
```
- **Lợi ích**: Giảm overhead, tăng throughput

### 5. **Packet Pacing** (Điều khiển tốc độ gửi)
- **Vấn đề**: Gửi quá nhanh gây tắc nghẽn và mất gói
- **Giải pháp**: Thêm delay giữa các gói tin
- **Code**:
```python
for packet in packets:
    send_packet(packet)
    time.sleep(0.001)  # 1ms delay
```
- **Lợi ích**: Giảm packet loss, tránh tắc nghẽn

### 6. **Adaptive Timeout** (Timeout thích ứng)
- **Vấn đề**: Timeout cố định không phù hợp với mọi điều kiện mạng
- **Giải pháp**: Điều chỉnh timeout dựa trên RTT đo được
- **Code**:
```python
def update_adaptive_timeout(self, rtt):
    avg_rtt = average(rtt_samples)
    self.timeout = max(0.5, min(5.0, avg_rtt * 2))
```
- **Lợi ích**: Tối ưu cho mọi điều kiện mạng

### 7. **Performance Monitoring** (Giám sát hiệu năng)
- **Theo dõi**: Packets/second, bytes/second, RTT, packet loss
- **Lợi ích**: Phát hiện vấn đề, tối ưu hiệu suất

### 8. **Concurrent Processing** (Xử lý đồng thời)
- **Giải pháp**: Sử dụng threading/asyncio
- **Lợi ích**: Tăng throughput, giảm latency

##  Cách Sử Dụng

### Bước 1: Chạy Server
```bash
python udp_optimized_server.py
```

Server sẽ:
- Lắng nghe trên port 9999
- Hiển thị thống kê mỗi 5 giây
- Xử lý các loại message: ping, data, stats

### Bước 2: Chạy Client (trong terminal khác)
```bash
python udp_optimized_client.py
```

Client sẽ chạy 4 test:
1. **Ping Test**: Đo RTT và test adaptive timeout
2. **Throughput Test**: Đo throughput với packet pacing
3. **Burst Test**: Test khả năng xử lý burst traffic
4. **Server Stats**: Lấy thống kê từ server

##  Kết Quả Mẫu

```
 TEST 1: PING TEST
Ping #1: RTT = 0.52ms, Timeout = 1.00s
Ping #2: RTT = 0.48ms, Timeout = 1.00s
...
 Kết quả:
  - RTT trung bình: 0.51ms
  - RTT min: 0.45ms
  - RTT max: 0.65ms
  - Packet loss: 0.0%

 TEST 2: THROUGHPUT TEST
 Kết quả:
  - Gói tin gửi: 50,000
  - Gói tin ACK: 49,850
  - Throughput: 10,240.50 KB/s
  - Packet rate: 10,000 pps
  - Delivery rate: 99.7%
```

