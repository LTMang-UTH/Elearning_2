using System;
using System.Net;
using System.Net.Sockets;
using System.Diagnostics;
using System.Threading;
using System.Collections.Generic;

class UdpClientDemo
{
    static string serverIp = "127.0.0.1";
    static int serverPort = 9999;

    static void Main()
    {
        Console.WriteLine("===== UDP OPTIMIZATION DEMO =====");
        Console.WriteLine("1 - Basic UDP");
        Console.WriteLine("2 - Optimized UDP");
        Console.Write("Chọn chế độ: ");

        string? choice = Console.ReadLine();

        if (choice == "1")
            RunBasic();
        else
            RunOptimized();
    }

    // ================= BASIC =================
    static void RunBasic()
    {
        Console.WriteLine("\n🔹 Chế độ BASIC UDP\n");

        UdpClient client = new UdpClient();
        client.Client.ReceiveTimeout = 1000;

        IPEndPoint serverEP = new IPEndPoint(IPAddress.Parse(serverIp), serverPort);

        int total = 100;
        int received = 0;
        List<double> rtts = new List<double>();

        for (int i = 0; i < total; i++)
        {
            byte[] packet = CreatePacket(i);
            Stopwatch sw = Stopwatch.StartNew();

            client.Send(packet, packet.Length, serverEP);

            try
            {
                client.Receive(ref serverEP);
                sw.Stop();
                rtts.Add(sw.Elapsed.TotalMilliseconds);
                received++;
            }
            catch
            {
                // Mất gói tin
            }
        }

        PrintStats(total, received, rtts);
    }

    // ================= OPTIMIZED =================
    static void RunOptimized()
    {
        Console.WriteLine("\n🚀 Chế độ OPTIMIZED UDP\n");

        UdpClient client = new UdpClient();

        // Tối ưu buffer gửi / nhận
        client.Client.SendBufferSize = 1024 * 1024;
        client.Client.ReceiveBufferSize = 1024 * 1024;

        client.Client.ReceiveTimeout = 500;

        IPEndPoint serverEP = new IPEndPoint(IPAddress.Parse(serverIp), serverPort);

        int total = 500;
        int received = 0;
        List<double> rtts = new List<double>();

        for (int i = 0; i < total; i++)
        {
            byte[] packet = CreatePacket(i);
            Stopwatch sw = Stopwatch.StartNew();

            client.Send(packet, packet.Length, serverEP);

            try
            {
                client.Receive(ref serverEP);
                sw.Stop();
                rtts.Add(sw.Elapsed.TotalMilliseconds);
                received++;
            }
            catch { }

            // Packet pacing – giới hạn tốc độ gửi
            Thread.Sleep(1);
        }

        PrintStats(total, received, rtts);
    }

    // ================= PACKET =================
    static byte[] CreatePacket(int id)
    {
        byte[] buffer = new byte[12];

        // int (4 bytes)
        Array.Copy(BitConverter.GetBytes(id), 0, buffer, 0, 4);

        // double (8 bytes)
        Array.Copy(BitConverter.GetBytes(GetTimestamp()), 0, buffer, 4, 8);

        return buffer;
    }

    static double GetTimestamp()
    {
        return DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() / 1000.0;
    }

    // ================= STATS =================
    static void PrintStats(int total, int received, List<double> rtts)
    {
        double loss = (total - received) * 100.0 / total;

        double avg = 0, min = double.MaxValue, max = 0;
        foreach (double r in rtts)
        {
            avg += r;
            if (r < min) min = r;
            if (r > max) max = r;
        }
        avg /= rtts.Count;

        double jitter = 0;
        foreach (double r in rtts)
            jitter += Math.Pow(r - avg, 2);
        jitter = Math.Sqrt(jitter / rtts.Count);

        Console.WriteLine("\n📊 KẾT QUẢ TEST:");
        Console.WriteLine($"- Tổng gói gửi: {total}");
        Console.WriteLine($"- Nhận được: {received}");
        Console.WriteLine($"- Loss rate: {loss:F2}%");
        Console.WriteLine($"- Avg RTT: {avg:F2} ms");
        Console.WriteLine($"- Min RTT: {min:F2} ms");
        Console.WriteLine($"- Max RTT: {max:F2} ms");
        Console.WriteLine($"- Jitter: {jitter:F2} ms");
    }
}
