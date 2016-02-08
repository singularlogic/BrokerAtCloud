@echo off
set curdir=%~dp0
call %curdir%\settings.bat

set FUSEKI_URL=http://%FUSEKI_HOST%:%FUSEKI_PORT%/%FUSEKI_SERVICE%/data?default
echo Appending to previous Fuseki contents...

set RDF_FILE="%BASEDIR%\var\TTL files\UC-esocc2015\BP_ESOCC2015_tutorial.ttl"
echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" %FUSEKI_URL%
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

set RDF_FILE="%BASEDIR%\var\TTL files\UC-esocc2015\SD-providerA-service1-GOLD.ttl"
echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" %FUSEKI_URL%
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

set RDF_FILE="%BASEDIR%\var\TTL files\UC-esocc2015\SD-providerB-service1-HIGH.ttl"
echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" %FUSEKI_URL%
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End


echo Loading completed with success

:End
echo loadrdf: Error-level: %errorlevel%

exit /b %errorlevel%