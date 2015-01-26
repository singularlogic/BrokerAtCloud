@echo off

rem set JVM="%JAVA_HOME%\bin\java.exe"
set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

rem set clpath="%BASEDIR%\target\classes;%BASEDIR%\target\dependency\*"
set clpath="%BASEDIR%\target\classes;%BASEDIR%\target\dependency\*;%BASEDIR%\target\OptWebapp\WEB-INF\lib\*"

set _argcActual=0
for %%i in (%*) do set /A _argcActual+=1

IF [%1]==[] goto :B
IF /I [%1]==[/n] goto :A
goto :B

:A
  shift
  @start cmd /c "java -DVERBOSE -classpath %clpath% eu.brokeratcloud.opt.engine.CLI %* && pause"
  goto :end
:B
  java -DVERBOSE -classpath %clpath% eu.brokeratcloud.opt.engine.CLI %*
:end
