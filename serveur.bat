:: compile-and-server.bat
@echo off
REM Compile tout et démarre le serveur

call compile.bat

REM Lancer le serveur avec les bibliothèques client WildFly 37
java -cp "echec-pong-server.jar;D:\wildfly-37.0.1.Final\bin\client\jboss-client.jar" server.ServerMain

pause