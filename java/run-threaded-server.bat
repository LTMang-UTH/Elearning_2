@echo off
chcp 65001 >nul

echo Chay Threaded Echo Server (Da luong)
echo.

call mvn -q exec:java ^
 -Dexec.mainClass=server.ThreadedEchoServer ^
 -Dexec.args=8889 ^
 -Dfile.encoding=UTF-8

pause
