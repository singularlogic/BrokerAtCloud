@echo off

rem set JVM="%JAVA_HOME%\bin\java.exe"
rem set JVM="C:\Program Files (x86)\Java\jdk1.7.0_07\bin\java.exe"
set JVM=java
set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."
set clpath="%BASEDIR%\target\classes;%BASEDIR%\target\dependency\*;%BASEDIR%\target\OptWebapp\WEB-INF\lib\*"

set _argcActual=0
for %%i in (%*) do set /A _argcActual+=1

IF [%1]==[] goto :B
IF /I [%1]==[/n] goto :A
goto :B

:A
  shift
  @start cmd /c "%JVM% -DVERBOSE -Xmx1000M -classpath %clpath% eu.brokeratcloud.opt.engine.CLI %* && pause"
  goto :end
:B
  %JVM% -DVERBOSE -Xmx1000M -classpath %clpath% eu.brokeratcloud.opt.engine.CLI %*
:end
