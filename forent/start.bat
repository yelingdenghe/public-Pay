@echo off
echo 啟動合租記賬系統前端服務...
echo.

REM 檢查Node.js環境
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 錯誤：未找到Node.js環境，請先安裝Node.js
    pause
    exit /b 1
)

REM 檢查npm環境
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 錯誤：未找到npm環境，請先安裝npm
    pause
    exit /b 1
)

echo 正在安裝依賴...
npm install

echo.
echo 正在啟動前端開發服務器...
echo 前端服務將在 http://localhost:3000 啟動
echo.

REM 啟動開發服務器
npm run dev

pause
