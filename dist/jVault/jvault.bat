@echo off

rem Batch file for jVault on Windows. 

:setjava
if "X%JAVA_HOME%" == "X" goto nojavahome

set JAVA="%JAVA_HOME%\bin\java"
goto run

:nojavahome
set JAVA=java

:run
set CLASSPATH=lib\commons-codec-1.4.jar;lib\commons-lang-2.5.jar;lib\jasypt-1.6.jar;bin\;lib\jvault.jar
%JAVA% ca/cogomov/jvault/jVault %1
