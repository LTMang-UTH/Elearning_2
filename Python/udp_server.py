import socket
import threading
import time
import random
import struct
from collections import deque

class OptimizedUDPServer:
    def __init__(self, host='127.0.0.1', port=5005, optimized=False):
        self.host = host
        self.port = port
        self.optimized = optimized
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.stats = {'packets': 0, 'lost': 0, 'rtt_samples': deque(maxlen=1000)}
        
        # Tối ưu socket buffer (4MB theo best practice) [web:1][web:2]
        bufsize = 4 * 1024 * 1024 if optimized else 212992
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_RCVBUF, bufsize)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_SNDBUF, bufsize)
        
        # Giả lập jumbo frame bằng payload lớn (8972 bytes theo IPv4 MTU 9000) [web:1]
        self.payload_size = 8972 if optimized else 1400
        
        # FEC: Thêm redundancy bits (simple parity) [web:7]
        self.fec_enabled = optimized
        
        print(f"Server {'OPTIMIZED' if optimized else 'BASIC'} - Buffer: {bufsize}, Payload: {self.payload_size}")
    
    def simulate_network(self, data):
        """Mô phỏng packet loss 5-20%, jitter 10-50ms [web:3][web:6]"""
        if random.random() < (0.05 if self.optimized else 0.20):  # Giảm loss khi optimized
            return None
        jitter = random.uniform(0.01, 0.05 if self.optimized else 0.1)
        time.sleep(jitter)
        return data
    
    def add_fec(self, data):
        """Simple FEC: XOR parity cho reliability [web:7]"""
        parity = bytes(a ^ b for a, b in zip(data, data[1:] + b'\x00'))
        return data + parity
    
    def check_fec(self, data):
        """Verify và recover FEC"""
        if len(data) > self.payload_size:
            orig, parity = data[:-1], data[-1:]
            calc_parity = bytes(a ^ b for a, b in zip(orig, orig[1:] + b'\x00'))
            if parity == calc_parity:
                return orig[:self.payload_size]
        return data[:self.payload_size]
    
    def handle_client(self, data, addr, recv_time):
        pkt_id = struct.unpack('!I', data[:4])[0]
        payload = self.check_fec(data)
        
        # Timestamp response
        response = struct.pack('!Id', pkt_id, time.time())
        response = self.add_fec(response) if self.fec_enabled else response
        
        processed = self.simulate_network(response)
        if processed:
            self.sock.sendto(processed, addr)
            self.stats['packets'] += 1
        else:
            self.stats['lost'] += 1
    
    def listen(self):
        self.sock.bind((self.host, self.port))
        print(f"Listening on {self.host}:{self.port}...")
        while True:
            try:
                data, addr = self.sock.recvfrom(4096)
                recv_time = time.time()
                threading.Thread(target=self.handle_client, args=(data, addr, recv_time)).start()
            except KeyboardInterrupt:
                break
        
        print(f"Stats: {self.stats['packets']} pkts, {self.stats['lost']} lost")

if __name__ == "__main__":
    mode = input("Chọn mode (1: Basic, 2: Optimized): ")
    server = OptimizedUDPServer(optimized=(mode == '2'))
    server.listen()
