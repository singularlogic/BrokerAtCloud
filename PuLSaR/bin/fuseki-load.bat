@echo off
set curdir=%~dp0
call %curdir%\settings.bat

set RDF_FILE="%1"

echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @"%RDF_FILE%" -H "Content-Type: text/turtle" http://%FUSEKI_HOST%:%FUSEKI_PORT%/%FUSEKI_SERVICE%/data?default
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

echo Loading completed with success

:End
echo loadrdf: Error-level: %errorlevel%

pause