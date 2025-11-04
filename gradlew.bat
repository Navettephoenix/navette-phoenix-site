@ECHO OFF

SETLOCAL

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS=-Xmx64m -Xms64m

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

set JAVACMD=
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVACMD=java.exe
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVACMD=%JAVA_HOME%in\java.exe

if exist "%JAVACMD%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.

PAUSE
exit /b 1

:execute

set CMD_LINE_ARGS=
set _SKIP=2

:parse
if "%1"=="" goto endparse

set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto parse

:endparse

"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%

ENDLOCAL
