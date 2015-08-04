@echo off

set curdir=%~dp0
set BASEDIR=%curdir:~0,-1%\..
set RDF_FILE="%BASEDIR%\var\TTL files + exports\cas-example-FINAL\2015-01-21-export-FINAL.ttl"
set CURL="%BASEDIR%\evaluation\curl-7.40.0-devel-mingw32\bin\curl.exe"

echo Previous Fuseki contents will be discarded...
echo Loading %RDF_FILE%...
%CURL% -X PUT --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

echo Loading completed with success

:End
echo loadrdf: Error-level: %errorlevel%

exit /b %errorlevel%