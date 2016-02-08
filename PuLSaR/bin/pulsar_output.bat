@echo off
set curdir=%~dp0
call %curdir%\settings.bat

cmd /C " cd %curdir%\.. && %JVM% %JETTY_LOGGING_LEVEL% -Dfile.encoding=UTF-8 -jar %BASEDIR%\bin\jetty-runner+ssi.jar --port %PULSAR_PORT% %PULSAR_CONTEXT% >%BASEDIR%\logs\OUTPUT.txt 2>&1 "
