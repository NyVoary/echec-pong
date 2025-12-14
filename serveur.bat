:: compile-and-server.bat
@echo off
REM Compile tout et démarre le serveur

call compile.bat

REM Lancer le serveur
java -cp "echec-pong-server.jar;lib/wildfly-client-all-27.0.1.Final.jar" server.ServerMain

pause