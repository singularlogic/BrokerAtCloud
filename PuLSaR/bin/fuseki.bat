@echo off

set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

@REM modify this to name the server jar
cmd /c "cd %BASEDIR%\fuseki & java -Xmx1000M -jar fuseki-server.jar -desc=tdb-assembler.ttl --update /BrokerAtCloudStore"
