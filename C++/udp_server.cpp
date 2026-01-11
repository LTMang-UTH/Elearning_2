#include <iostream>
#include <winsock2.h>
#pragma comment(lib, "ws2_32.lib")

#define PORT 8080
#define BUF_SIZE 1024

int main() {
    WSADATA wsa;
    SOCKET server;
    sockaddr_in addr, client;
    int clientLen = sizeof(client);
    char buffer[BUF_SIZE];

    WSAStartup(MAKEWORD(2,2), &wsa);

    server = socket(AF_INET, SOCK_DGRAM, 0);

    int bufSize = 65536;
    setsockopt(server, SOL_SOCKET, SO_RCVBUF, (char*)&bufSize, sizeof(bufSize));

    addr.sin_family = AF_INET;
    addr.sin_port = htons(PORT);
    addr.sin_addr.s_addr = INADDR_ANY;

    bind(server, (sockaddr*)&addr, sizeof(addr));

    std::cout << "UDP Server running...\n";

    while (true) {
        int recvLen = recvfrom(server, buffer, BUF_SIZE, 0,
                                (sockaddr*)&client, &clientLen);
        buffer[recvLen] = '\0';
        std::cout << "Client: " << buffer << std::endl;
    }

    closesocket(server);
    WSACleanup();
    return 0;
}
