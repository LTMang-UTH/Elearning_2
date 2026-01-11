#include <iostream>
#include <winsock2.h>
#pragma comment(lib, "ws2_32.lib")

#define PORT 8080
#define BUF_SIZE 1024

int main() {
    WSADATA wsa;
    SOCKET client;
    sockaddr_in server;
    char buffer[BUF_SIZE];

    WSAStartup(MAKEWORD(2,2), &wsa);

    client = socket(AF_INET, SOCK_DGRAM, 0);

    int bufSize = 65536;
    setsockopt(client, SOL_SOCKET, SO_SNDBUF, (char*)&bufSize, sizeof(bufSize));

    server.sin_family = AF_INET;
    server.sin_port = htons(PORT);
    server.sin_addr.s_addr = inet_addr("127.0.0.1");

    while (true) {
        std::cout << "You: ";
        std::cin.getline(buffer, BUF_SIZE);

        sendto(client, buffer, strlen(buffer), 0,
               (sockaddr*)&server, sizeof(server));
    }

    closesocket(client);
    WSACleanup();
    return 0;
}
