@echo off

set curdir=%~dp0
set BASEDIR="%curdir:~0,-1%\.."

set path=%path%;bin
cmd /k "cd %BASEDIR%"
