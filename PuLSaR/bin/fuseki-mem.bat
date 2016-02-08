@echo off
set curdir=%~dp0
call %curdir%\settings.bat

cmd /c "cd %BASEDIR%\fuseki & %JVM% -Xmx1000M -jar fuseki-server.jar --mem --update --port=%FUSEKI_PORT% /%FUSEKI_SERVICE% "

pause