@echo off
set BASEDIR=%~dp0
set sep=/
call set DIR2=%%BASEDIR:\=%sep%%%

type %BASEDIR%..\fuseki\tdb-assembler.ttl.TEMPLATE > %BASEDIR%..\fuseki\tdb-assembler.ttl
echo     tdb:location "%DIR2%../fuseki/DB" ; >> %BASEDIR%..\fuseki\tdb-assembler.ttl
echo     . >> %BASEDIR%..\fuseki\tdb-assembler.ttl
echo Done