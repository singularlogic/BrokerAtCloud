@echo off
set curdir=%~dp0
call %curdir%\settings.bat

set clpath="%BASEDIR%\target\classes;%BASEDIR%\target\dependency\*;%BASEDIR%\target\PuLSaR\WEB-INF\lib\*"

set _argcActual=0
for %%i in (%*) do set /A _argcActual+=1

IF [%1]==[] goto :B
IF /I [%1]==[/n] goto :A
goto :B

:A
  shift
  @start cmd /c "%JVM% %JETTY_LOGGING_LEVEL% -Xmx1000M -classpath %clpath% eu.brokeratcloud.opt.engine.CLI %* && pause"
  goto :end
:B
  %JVM% %JETTY_LOGGING_LEVEL% -Xmx1000M -classpath %clpath% eu.brokeratcloud.opt.engine.CLI %*
:end
