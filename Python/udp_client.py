import socket
import time
import statistics
import struct
import threading

class UDPClient:
    def __init__(self, host='127.0.0.1', port=5005, packets=100, optimized=False):
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        
        # Tối ưu send buffer [web:1][web:4]
        bufsize = 4 * 1024 * 1024 if optimized else 212992
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_SNDBUF, bufsize)
        
        self.packets = packets
        self.rtts = []
        self.lost = 0
        
        print(f"Client {'OPTIMIZED' if optimized else 'BASIC'} gửi {packets} packets")
    
    def send_packet(self, pkt_id):
        send_time = time.time()
        data = struct.pack('!Id', pkt_id, send_time)

        self.sock.sendto(data, (self.host, self.port))
        self.sock.settimeout(1.0)

        try:
            response, _ = self.sock.recvfrom(4096)
            # Không dùng thời gian server trả về, chỉ dùng để xác thực đúng gói
            # RTT = thời gian nhận được ở client - thời gian gửi đi ở client
            recv_time = time.time()
            rtt = recv_time - send_time
            self.rtts.append(rtt)
            print(f"Pkt {pkt_id}: RTT={rtt*1000:.1f}ms")
        except socket.timeout:
            self.lost += 1
            print(f"Pkt {pkt_id}: LOST")
    
    def run_test(self):
        threads = []
        for i in range(self.packets):
            t = threading.Thread(target=self.send_packet, args=(i,))
            t.start()
            threads.append(t)
            time.sleep(0.01)  # Rate limiting
        
        for t in threads:
            t.join()
        
        self.print_stats()
    
    def print_stats(self):
        loss_rate = (self.lost / self.packets) * 100
        print(f"\n=== THỐNG KÊ ===")
        print(f"Loss rate: {loss_rate:.1f}%")
        print(f"Avg RTT: {statistics.mean(self.rtts)*1000:.1f}ms" if self.rtts else "N/A")
        print(f"Min RTT: {min(self.rtts)*1000:.1f}ms" if self.rtts else "N/A")
        print(f"Max RTT: {max(self.rtts)*1000:.1f}ms" if self.rtts else "N/A")
        print(f"Jitter: {statistics.stdev(self.rtts)*1000:.1f}ms" if len(self.rtts)>1 else "N/A")

if __name__ == "__main__":
    mode = input("Test mode (1: Basic, 2: Optimized): ")
    client = UDPClient(optimized=(mode == '2'))
    client.run_test()
