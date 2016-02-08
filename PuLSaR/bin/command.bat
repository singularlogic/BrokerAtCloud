@echo off
call settings.bat

set path=%path%;"%BASEDIR%\bin"
cmd /k "cd %BASEDIR%"
