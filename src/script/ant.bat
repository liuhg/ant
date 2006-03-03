@echo off

REM  Copyright 2001,2004-2006 The Apache Software Foundation
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

REM This is an inordinately troublesome piece of code, particularly because it
REM tries to work on both Win9x and WinNT-based systems. If we could abandon '9x
REM support, things would be much easier, but sadly, it is not yet time.
REM Be cautious about editing this, and only add WinNT specific stuff in code that
REM only runs on WinNT.

if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_ANT_HOME=%~dp0..

if "%ANT_HOME%"=="" set ANT_HOME=%DEFAULT_ANT_HOME%
set DEFAULT_ANT_HOME=

set _USE_CLASSPATH=yes

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set ANT_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
if ""%1""==""-noclasspath"" goto clearclasspath
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
shift
goto setupArgs

rem here is there is a -noclasspath in the options
:clearclasspath
set _USE_CLASSPATH=no
shift
goto setupArgs

rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart
rem find ANT_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%ANT_HOME%\lib\ant.jar" goto checkJava

rem check for ant in Program Files
if not exist "%ProgramFiles%\ant" goto checkSystemDrive
set ANT_HOME=%ProgramFiles%\ant
goto checkJava

:checkSystemDrive
rem check for ant in root directory of system drive
if not exist %SystemDrive%\ant\lib\ant.jar goto checkCDrive
set ANT_HOME=%SystemDrive%\ant
goto checkJava

:checkCDrive
rem check for ant in C:\ant for Win9X users
if not exist C:\ant\lib\ant.jar goto noAntHome
set ANT_HOME=C:\ant
goto checkJava

:noAntHome
echo ANT_HOME is set incorrectly or ant could not be located. Please set ANT_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto checkJikes

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:checkJikes
if not "%JIKESPATH%"=="" goto runAntWithJikes

:runAnt
if "%_USE_CLASSPATH%"=="no" goto runAntNoClasspath
if not "%CLASSPATH%"=="" goto runAntWithClasspath
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar" "-Dant.home=%ANT_HOME%" org.apache.tools.ant.launch.Launcher %ANT_ARGS% %ANT_CMD_LINE_ARGS%
rem Check the error code of the Ant build
if not "%OS%"=="Windows_NT" goto onError
set ANT_ERROR=%ERRORLEVEL%
goto end

:runAntNoClasspath
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar" "-Dant.home=%ANT_HOME%" org.apache.tools.ant.launch.Launcher %ANT_ARGS% %ANT_CMD_LINE_ARGS%
rem Check the error code of the Ant build
if not "%OS%"=="Windows_NT" goto onError
set ANT_ERROR=%ERRORLEVEL%
goto end

:runAntWithClasspath
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar" "-Dant.home=%ANT_HOME%" org.apache.tools.ant.launch.Launcher %ANT_ARGS% -cp "%CLASSPATH%" %ANT_CMD_LINE_ARGS%
rem Check the error code of the Ant build
if not "%OS%"=="Windows_NT" goto onError
set ANT_ERROR=%ERRORLEVEL%
goto end

:runAntWithJikes
if "%_USE_CLASSPATH%"=="no" goto runAntWithJikesNoClasspath
if not "%CLASSPATH%"=="" goto runAntWithJikesAndClasspath

:runAntWithJikesNoClasspath
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar" "-Dant.home=%ANT_HOME%" "-Djikes.class.path=%JIKESPATH%" org.apache.tools.ant.launch.Launcher %ANT_ARGS% %ANT_CMD_LINE_ARGS%
rem Check the error code of the Ant build
if not "%OS%"=="Windows_NT" goto onError
set ANT_ERROR=%ERRORLEVEL%
goto end

:runAntWithJikesAndClasspath
"%_JAVACMD%" %ANT_OPTS% -classpath "%ANT_HOME%\lib\ant-launcher.jar" "-Dant.home=%ANT_HOME%" "-Djikes.class.path=%JIKESPATH%" org.apache.tools.ant.launch.Launcher %ANT_ARGS%  -cp "%CLASSPATH%" %ANT_CMD_LINE_ARGS%
rem Check the error code of the Ant build
if not "%OS%"=="Windows_NT" goto onError
set ANT_ERROR=%ERRORLEVEL%
goto end

:onError
rem Windows 9x way of checking the error code.  It matches via brute force.
for %%i in (1 10 100) do set err%%i=
for %%i in (0 1 2) do if errorlevel %%i00 set err100=%%i
if %err100%==2 goto onError200
if %err100%==0 set err100=
for %%i in (0 1 2 3 4 5 6 7 8 9) do if errorlevel %err100%%%i0 set err10=%%i
if "%err100%"=="" if %err10%==0 set err10=
:onError1
for %%i in (0 1 2 3 4 5 6 7 8 9) do if errorlevel %err100%%err10%%%i set err1=%%i
goto onErrorEnd
:onError200
for %%i in (0 1 2 3 4 5) do if errorlevel 2%%i0 set err10=%%i
if err10==5 for %%i in (0 1 2 3 4 5) do if errorlevel 25%%i set err1=%%i
if not err10==5 goto onError1
:onErrorEnd
set ANT_ERROR=%err100%%err10%%err1%
for %%i in (1 10 100) do set err%%i=

:end
rem bug ID 32069: resetting an undefined env variable changes the errorlevel.
set _JAVACMD=DUMMY_VAL
set _JAVACMD=
set ANT_CMD_LINE_ARGS=DUMMY_VAL
set ANT_CMD_LINE_ARGS=

rem Set the return code if we are not in NT.  We can only set
rem a value of 1, but it's better than nothing.
if not "%OS%"=="Windows_NT" if "%ANT_ERROR%"=="" set ANT_ERROR=255
if not "%OS%"=="Windows_NT" if "%ANT_ERROR%"=="0" goto quit
if not "%OS%"=="Windows_NT" echo 1 > nul | choice /n /c:1
rem Set the ERRORLEVEL if we are running NT.
if "%OS%"=="Windows_NT" if "%ANT_ERROR%"=="" set ANT_ERROR=255
if "%OS%"=="Windows_NT" if not %ANT_ERROR%==0 color 00
goto quit

rem If there were no errors, we run the post script.
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:mainEnd
if exist "%HOME%\antrc_post.bat" call "%HOME%\antrc_post.bat"

:quit

