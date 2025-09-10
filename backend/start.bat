@echo off
echo 啟動合租記賬系統後端服務...
echo.

REM 檢查Java環境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 錯誤：未找到Java環境，請先安裝Java 17或更高版本
    pause
    exit /b 1
)

REM 檢查Maven環境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 錯誤：未找到Maven環境，請先安裝Maven
    pause
    exit /b 1
)

echo 正在編譯和啟動Spring Boot應用...
echo.

REM 編譯並啟動應用
mvn spring-boot:run

pause
