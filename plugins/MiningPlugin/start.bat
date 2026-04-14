@echo off
title Mining Server
color 0A

:: Mob ve köy spawn'ını kapat
powershell -Command "(gc server.properties) -replace 'spawn-monsters=.*', 'spawn-monsters=false' | Set-Content server.properties"
powershell -Command "(gc server.properties) -replace 'spawn-animals=.*', 'spawn-animals=false' | Set-Content server.properties"
powershell -Command "(gc server.properties) -replace 'generate-structures=.*', 'generate-structures=false' | Set-Content server.properties"

echo Mining Server baslatiliyor...
java -Xmx2G -Xms1G -jar paper.jar nogui

echo.
echo Server kapandi. Yeniden baslatmak icin bir tusa bas...
pause
