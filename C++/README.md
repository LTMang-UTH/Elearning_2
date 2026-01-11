# UDP Optimization Demo (C++)

## Mục tiêu
Mô phỏng giao thức UDP và minh họa các kỹ thuật tối ưu hóa:
- Tăng buffer size
- Giảm độ trễ
- Truyền dữ liệu nhanh, không handshake

## File
- udp_server.cpp: UDP Server
- udp_client.cpp: UDP Client

## Cách chạy
g++ udp_server.cpp -o udp_server -lws2_32  
g++ udp_client.cpp -o udp_client -lws2_32  

Chạy server trước, sau đó chạy client.
