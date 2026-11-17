@echo off
setlocal
cd /d "%~dp0"
echo ============================================================
echo  CareerBridge database viewer (H2 Console) - opens in browser
echo  On the login page type exactly:
echo    JDBC URL : jdbc:h2:file:%CD:\=/%/data/careerbridge;AUTO_SERVER=TRUE
echo    User Name: sa
echo    Password : (leave empty)
echo  Then click Connect and run:  SELECT * FROM users;
echo  (The website can stay running while you do this.)
echo ============================================================
java -cp lib\h2.jar org.h2.tools.Console
pause
