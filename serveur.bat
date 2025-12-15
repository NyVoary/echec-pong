:: compile-and-server.bat
@echo off
REM Compile tout et démarre le serveur

set WILDFLY_HOME=D:\wildfly-37.0.1.Final

call compile.bat

REM Lancer le serveur avec les libs WildFly
java -cp "echec-pong-server.jar;%WILDFLY_HOME%\bin\client\jboss-client.jar" server.ServerMain

pause