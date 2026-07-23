@echo off
setlocal EnableExtensions

set "PROJECT_DIR=%~dp0"
set "GRADLE_USER_HOME=%PROJECT_DIR%.test-env\gradle-home"

for /d %%D in ("%PROJECT_DIR%.test-env\jdk-*") do (
	if exist "%%~fD\bin\java.exe" set "JAVA_HOME=%%~fD"
)

if not defined JAVA_HOME (
	echo ERROR: The project-local Java 25 runtime was not found in .test-env.
	exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

if "%~1"=="" (
	call "%PROJECT_DIR%gradlew.bat" build
) else (
	call "%PROJECT_DIR%gradlew.bat" %*
)

exit /b %ERRORLEVEL%
