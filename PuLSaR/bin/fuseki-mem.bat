@echo off

set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

cmd /c "cd %BASEDIR%\fuseki & java -Xmx1000M -jar fuseki-server.jar --mem --update /BrokerAtCloudStore "

pause