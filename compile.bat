@echo off
REM Nettoyage du dossier de sortie
if exist out rmdir /s /q out
mkdir out

REM Compilation des sources
javac -d out common/*.java server/*.java client/*.java

REM Copie les images dans le dossier de sortie
xcopy pieces out\pieces /E /I /Y

REM === JAR SERVEUR ===
echo Main-Class: server.ServerMain > out/MANIFEST_SERVER.MF
jar cfm echec-pong-server.jar out/MANIFEST_SERVER.MF -C out . 

REM === JAR CLIENT ===
echo Main-Class: client.ClientMain > out/MANIFEST_CLIENT.MF
jar cfm echec-pong-client.jar out/MANIFEST_CLIENT.MF -C out .

echo.
echo ========================================
echo Compilation terminee !
echo ========================================
echo Fichiers crees :
echo   - echec-pong-server.jar (pour heberger)
echo   - echec-pong-client.jar (pour jouer)
echo ========================================