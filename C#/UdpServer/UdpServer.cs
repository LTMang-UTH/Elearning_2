using System;
using System.Net;
using System.Net.Sockets;

class UdpServer
{
    static void Main()
    {
        int port = 9999;
        UdpClient server = new UdpClient(port);

        // Tối ưu buffer nhận
        server.Client.ReceiveBufferSize = 1024 * 1024;

        Console.WriteLine("🚀 UDP Server đang chạy tại port 9999...");

        IPEndPoint clientEP = new IPEndPoint(IPAddress.Any, 0);

        while (true)
        {
            byte[] data = server.Receive(ref clientEP);

            // Echo lại đúng gói tin nhận được
            server.Send(data, data.Length, clientEP);
        }
    }
}
