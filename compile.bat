@echo off
REM Nettoyage du dossier de sortie
if exist out rmdir /s /q out
mkdir out

REM Compilation des sources
javac -d out common/*.java server/*.java client/*.java

REM Création du manifest pour le JAR
echo Main-Class: server.ServerMain > out/MANIFEST.MF

REM Création du JAR
jar cfm echec-pong.jar out/MANIFEST.MF -C out .

echo Compilation et création du JAR terminées.