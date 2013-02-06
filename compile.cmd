@echo off
set DIR=%~dp0
set JAVA=java
set JAR=%DIR%target\ScaladocCHM.jar
set OPTS=

:GETOPTS
if not "%1"=="" goto COMPILE
goto HELP

:HELP
echo Usage:
echo $ %0 [directory]
echo $ %0 scala-docs-2.10.0
goto EOL

:COMPILE
%JAVA% %OPTS% -cp %JAR% kr.co.hkcb.tools.chm.CompileScala %~1
goto EOL

:EOL
