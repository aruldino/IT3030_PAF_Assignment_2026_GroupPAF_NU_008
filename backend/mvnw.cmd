@echo off
setlocal

set "MVN_VERSION=3.9.9"
set "BASE_DIR=%~dp0"
set "MAVEN_CACHE_DIR=%BASE_DIR%.mvn"
set "MAVEN_HOME=%MAVEN_CACHE_DIR%\apache-maven-%MVN_VERSION%"
set "MAVEN_ARCHIVE=%MAVEN_CACHE_DIR%\apache-maven-%MVN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip"

if exist "%BASE_DIR%.env" (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%BASE_DIR%.env") do (
        if not "%%A"=="" set "%%A=%%B"
    )
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    if not exist "%MAVEN_CACHE_DIR%" mkdir "%MAVEN_CACHE_DIR%"

    if not exist "%MAVEN_ARCHIVE%" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ARCHIVE%'"
        if errorlevel 1 exit /b 1
    )

    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%MAVEN_ARCHIVE%' -DestinationPath '%MAVEN_CACHE_DIR%' -Force"
    if errorlevel 1 exit /b 1
)

"%MAVEN_HOME%\bin\mvn.cmd" -f "%BASE_DIR%pom.xml" %*