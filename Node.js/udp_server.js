const dgram = require("dgram");

// Tạo socket UDP với các tùy chọn tối ưu
const server = dgram.createSocket({
  type: "udp4",
  reuseAddr: true, // Hữu ích khi restart server nhanh
});

server.on("error", (err) => {
  console.error(`Server error:\n${err.stack}`);
  server.close();
});

server.on("message", (msg, rinfo) => {
  // Xử lý nhanh: Echo uppercase để dễ thấy
  const response = Buffer.from(msg.toString().toUpperCase());

  server.send(response, rinfo.port, rinfo.address, (err) => {
    if (err) console.error("Send error:", err);
  });

  console.log(
    `Received ${msg.length} bytes from ${rinfo.address}:${rinfo.port} → Echoed back`
  );
});

server.on("listening", () => {
  const addr = server.address();
  console.log(
    `🚀 UDP Echo Server đang lắng nghe trên ${addr.address}:${addr.port}`
  );

  // Log buffer thực tế sau khi bind (OS có thể điều chỉnh xuống)
  console.log(
    `   Buffer thực tế: recv=${formatBytes(
      server.getRecvBufferSize()
    )} send=${formatBytes(server.getSendBufferSize())}`
  );
});

// Hàm format bytes đẹp (tùy chọn)
function formatBytes(bytes) {
  if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  if (bytes >= 1024) return (bytes / 1024).toFixed(1) + " KB";
  return bytes + " bytes";
}

server.bind(10001, "localhost");
