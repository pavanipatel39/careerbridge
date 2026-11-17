@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo JDK is NOT installed. Install "Temurin JDK 21" from https://adoptium.net then run again.
  pause
  exit /b 1
)
javac -version
if exist backend\target\classes rmdir /s /q backend\target\classes
if exist backend\target\test-classes rmdir /s /q backend\target\test-classes
if exist test-reports rmdir /s /q test-reports
mkdir backend\target\classes
mkdir backend\target\test-classes
mkdir test-reports
rem Relative paths in the file lists, so folders with spaces (like "new folder") work
for /r backend\src\main\java %%f in (*.java) do (
  set "p=%%f"
  echo !p:%CD%\=!>>test-reports\main-sources.txt
)
for /r backend\src\test\java %%f in (*.java) do (
  set "p=%%f"
  echo !p:%CD%\=!>>test-reports\test-sources.txt
)
javac --release 17 -encoding UTF-8 -d backend\target\classes @test-reports\main-sources.txt
if errorlevel 1 ( echo Main code compile failed & pause & exit /b 1 )
javac --release 17 -encoding UTF-8 -cp "backend\target\classes;lib-test\junit-console.jar;lib\h2.jar" -d backend\target\test-classes @test-reports\test-sources.txt
if errorlevel 1 ( echo Test code compile failed & pause & exit /b 1 )
echo Running tests...
java -DDATA_DIR=test-reports\data -javaagent:lib-test\jacocoagent.jar=destfile=test-reports\jacoco.exec,includes=com.careerbridge.* -jar lib-test\junit-console.jar --class-path "backend\target\classes;backend\target\test-classes;lib\h2.jar" --scan-class-path backend\target\test-classes --disable-banner --disable-ansi-colors --details-theme=ascii --details=tree --reports-dir test-reports\junit > test-reports\test-output.txt 2>&1
findstr /C:"tests successful" /C:"tests failed" /C:"tests found" test-reports\test-output.txt
javac --release 17 -cp "lib-test\*" -d tools tools\CoverageReport.java
if errorlevel 1 ( echo Coverage tool compile failed & pause & exit /b 1 )
java -cp "tools;lib-test\*" CoverageReport test-reports\jacoco.exec backend\target\classes backend\src\main\java test-reports\coverage
if errorlevel 1 ( echo Coverage report failed - send a screenshot of this window & pause & exit /b 1 )
echo.
echo DONE. Open test-reports\coverage\html\index.html in Chrome for the coverage report.
echo Test output is saved in test-reports\test-output.txt  (take screenshots for your Testing report)
pause
