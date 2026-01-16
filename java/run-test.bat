@echo off
chcp 65001 >nul
echo Test với nhiều client đồng thời
echo.
echo Lưu ý: Đảm bảo server đã chạy trước khi test!
echo.
if "%1"=="" (
    call mvn -q exec:java -Dexec.mainClass=client.MultiClientTest -Dexec.args="localhost 8888 100" -Dfile.encoding=UTF-8
) else if "%2"=="" (
    call mvn -q exec:java -Dexec.mainClass=client.MultiClientTest -Dexec.args="%1 8888 100" -Dfile.encoding=UTF-8
) else if "%3"=="" (
    call mvn -q exec:java -Dexec.mainClass=client.MultiClientTest -Dexec.args="%1 %2 100" -Dfile.encoding=UTF-8
) else (
    call mvn -q exec:java -Dexec.mainClass=client.MultiClientTest -Dexec.args="%1 %2 %3" -Dfile.encoding=UTF-8
)
pause

