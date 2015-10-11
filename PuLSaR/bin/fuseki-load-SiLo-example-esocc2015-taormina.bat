@echo off

set curdir=%~dp0
set BASEDIR=%curdir:~0,-1%\..
set CURL="%BASEDIR%\evaluation\curl\curl.exe"

echo Appending to previous Fuseki contents...

set RDF_FILE="%BASEDIR%\var\TTL files\UC SiLo - esocc2015 tutorial\preparation\20150904_BP_ESOCC2015_tutorial_v4 + SMI-attr-mappings.ttl"
echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

set RDF_FILE="%BASEDIR%\var\TTL files\UC SiLo - esocc2015 tutorial\preparation\SD-providerA-service1-GOLD.ttl"
echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End

set RDF_FILE="%BASEDIR%\var\TTL files\UC SiLo - esocc2015 tutorial\preparation\SD-providerB-service1-HIGH.ttl"
echo Loading %RDF_FILE%...
%CURL% -X POST --data-binary @%RDF_FILE% -H "Content-Type: text/turtle" http://localhost:3030/BrokerAtCloudStore/data?default
IF ERRORLEVEL 1 goto End
IF NOT ERRORLEVEL 0 goto End


echo Loading completed with success

:End
echo loadrdf: Error-level: %errorlevel%

exit /b %errorlevel%