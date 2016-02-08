@echo off
set curdir=%~dp0
call %curdir%\settings.bat

@REM modify this to name the server jar
cmd /c "cd %BASEDIR%\fuseki & %JVM% -Xmx1000M -jar fuseki-server.jar -desc=tdb-assembler.ttl --update --port=%FUSEKI_PORT% /%FUSEKI_SERVICE%"
