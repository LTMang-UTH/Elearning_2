@echo off
chcp 65001 >nul
echo Chạy Echo Client
echo.
if "%1"=="" (
    call mvn -q exec:java -Dexec.mainClass=client.EchoClient -Dexec.args="localhost 8888" -Dfile.encoding=UTF-8
) else if "%2"=="" (
    call mvn -q exec:java -Dexec.mainClass=client.EchoClient -Dexec.args="%1 8888" -Dfile.encoding=UTF-8
) else (
    call mvn -q exec:java -Dexec.mainClass=client.EchoClient -Dexec.args="%1 %2" -Dfile.encoding=UTF-8
)
pause
