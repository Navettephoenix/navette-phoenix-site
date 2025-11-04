@ECHO OFF

SETLOCAL

set DIR=%~dp0
IF "%DIR%" == "" set DIR=.
SET APP_BASE_NAME=%~n0
SET APP_HOME=%DIR%

SET DEFAULT_JVM_OPTS=

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

SET JAVA_EXE=java.exe
IF DEFINED JAVA_HOME (
    SET JAVA_EXE=%JAVA_HOME%in\java.exe
)

IF NOT EXIST "%JAVA_EXE%" (
    ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    EXIT /B 1
)

set CMD_LINE_ARGS=
:arg_loop
IF "%1"=="" GOTO execute
    set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
    SHIFT
    GOTO arg_loop

:execute
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%
