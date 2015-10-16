@echo off

set JVM="%JAVA_HOME%\bin\java.exe"
set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

@start cmd /c " %JVM% -DVERBOSE -Dfile.encoding=UTF-8 -jar %BASEDIR%\bin\jetty-runner+ssi.jar --port 9090 %BASEDIR%\target\PuLSaR.war "

rem jetty logging options: -DDEBUG  -DVERBOSE -DIGNORED