@echo off
chcp 65001 >nul
echo Chạy NIO Echo Server (Non-blocking I/O với Selector)
echo.
call mvn -q exec:java -Dexec.mainClass=server.NIOEchoServer -Dexec.args=8888 -Dfile.encoding=UTF-8
pause
