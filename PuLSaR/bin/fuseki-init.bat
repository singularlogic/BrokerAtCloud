@echo off

set curdir=%~dp0
set BASEDIR=%curdir:~0,-1%\..
set RDF_FILE="%BASEDIR%\var\TTL files\03. ontologies (gr+usdl-core+sla-cb+prefs+smi v2).ttl"
set CURL="%BASEDIR%\bin\curl\curl.exe"

echo Previous Fuseki contents will be discarded...
echo Loading %RDF_FILE%...
%CURL% -X PUT --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

echo Loading completed with success

:End
echo loadrdf: Error-level: %errorlevel%

exit /b %errorlevel%