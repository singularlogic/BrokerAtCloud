rem THIS FILE CONTAINS PULSAR, FUSEKI AND CLI LAUNCH SETTINGS
rem THIS FILE IS USED BY ALL OTHER .BAT FILES

rem Basic folders
set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

rem Java executables
rem set JVM="C:\Program Files\Java\jdk1.8.0_74\bin\java.exe"
set JVM="%JAVA_HOME%\bin\java.exe"
rem set JVM=java

rem cURL executable
set CURL="%BASEDIR%\bin\curl\curl.exe"
rem set CURL=curl

rem PuLSaR settings
set PULSAR_PORT=9090
set PULSAR_CONTEXT=%BASEDIR%\target\PuLSaR\
rem set PULSAR_CONTEXT=%BASEDIR%\target\PuLSaR.war
rem set PULSAR_CONTEXT=%BASEDIR%\bin\PuLSaR.war

rem Jetty settings
set JETTY_LOGGING_LEVEL=-DVERBOSE
rem options: -DDEBUG  -DVERBOSE -DIGNORED

rem Fuseki settings
set FUSEKI_SERVICE=BrokerAtCloudStore
set FUSEKI_HOST=localhost
set FUSEKI_PORT=3030