@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

rem ---- OPTIONAL: enable "Continue with Google" (see README, section "Google login") ----
rem set GOOGLE_CLIENT_ID=PASTE-YOUR-CLIENT-ID.apps.googleusercontent.com

where javac >nul 2>nul
if errorlevel 1 (
  echo JDK is NOT installed. Install "Temurin JDK 21" from https://adoptium.net then close this window and run again.
  pause
  exit /b 1
)
netstat -ano | findstr /R /C:":8080 .*LISTENING" >nul
if not errorlevel 1 (
  echo Port 8080 is already used by an OLD server. Close every old black server window ^(or restart the laptop^) and run again.
  pause
  exit /b 1
)

if not exist backend\target\classes mkdir backend\target\classes
if exist backend\sources.txt del backend\sources.txt
rem Relative paths in the file list, so folders with spaces (like "new folder") work
for /r backend\src\main\java %%f in (*.java) do (
  set "p=%%f"
  echo !p:%CD%\=!>>backend\sources.txt
)
echo Compiling CareerBridge Java backend...
javac -encoding UTF-8 -d backend\target\classes @backend\sources.txt
if errorlevel 1 (
  echo Compilation failed. Check that JDK 17 or newer is installed and on PATH.
  pause
  exit /b 1
)
echo Starting CareerBridge at http://localhost:8080
java -cp "backend\target\classes;lib\h2.jar" com.careerbridge.Main
pause
