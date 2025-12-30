const dgram = require("dgram");
const readline = require("readline");

// Tạo socket UDP
const client = dgram.createSocket("udp4");

// Cấu hình server
const SERVER_PORT = 10001;
const SERVER_HOST = "localhost";

// Tạo interface đọc input
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  prompt: "UDP Client > ",
});

console.log(`🚀 UDP Interactive Client → ${SERVER_HOST}:${SERVER_PORT}`);
console.log("Gõ tin nhắn + Enter để gửi. Gõ 'quit' hoặc 'exit' để thoát.\n");

// *** QUAN TRỌNG: Bind socket để có local port → nhận được echo từ server ***
client.bind(0); // 0 = hệ thống tự chọn port ngẫu nhiên

rl.prompt();

rl.on("line", (line) => {
  const message = line.trim();

  if (message === "quit" || message === "exit") {
    console.log("Đang đóng client...");
    client.close();
    rl.close();
    return;
  }

  if (message === "") {
    rl.prompt();
    return;
  }

  client.send(Buffer.from(message), SERVER_PORT, SERVER_HOST, (err) => {
    if (err) {
      console.error("Lỗi gửi:", err);
      client.close();
    } else {
      console.log(`Đã gửi: ${message}`);
    }
  });

  rl.prompt();
});

// Nhận echo từ server
client.on("message", (msg, rinfo) => {
  console.log(
    `\n📨 Echo từ server (${rinfo.address}:${rinfo.port}): ${msg.toString()}`
  );
  rl.prompt();
});

client.on("error", (err) => {
  console.error(`Client error:\n${err.stack}`);
  client.close();
});

client.on("close", () => {
  console.log("\nClient đã đóng.");
  rl.close();
});
