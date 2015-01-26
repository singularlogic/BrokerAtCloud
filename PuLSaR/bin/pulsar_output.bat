@echo off

set JVM="%JAVA_HOME%\bin\java.exe"
set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

%JVM% -DVERBOSE -jar %BASEDIR%\bin\jetty-runner+ssi.jar --port 9090 %BASEDIR%\target\PuLSaR.war >%BASEDIR%\OUTPUT.txt 2>&1
