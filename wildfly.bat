:: start-wildfly.bat
@echo off
REM Démarre WildFly et déploie le jar EJB

REM Adapter ce chemin si besoin
set WILDFLY_HOME=C:\wildfly-37.0.1.Final

REM Démarrer WildFly en arrière-plan (ouvre une nouvelle fenêtre)
start "" "%WILDFLY_HOME%\bin\standalone.bat"

REM Attendre quelques secondes que WildFly démarre (optionnel)
timeout /t 10

REM === BUILD EJB AVEC MAVEN ===
cd ejb
call mvn clean package
cd ..

REM === COPIE DU JAR EJB DANS WILDFLY ===
copy /Y ejb\target\configservice-1.0-SNAPSHOT.jar "%WILDFLY_HOME%\standalone\deployments\configservice.jar"

echo.
echo ========================================
echo WildFly démarré et configservice.jar déployé !
echo ========================================
pause