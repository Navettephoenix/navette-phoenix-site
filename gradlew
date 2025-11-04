#!/usr/bin/env sh
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="$(command -v java 2>/dev/null || true)"
fi

if [ ! -x "$JAVA_BIN" ]; then
  echo "ERROR: Java runtime not found. Set JAVA_HOME or install Java." >&2
  exit 1
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Gradle wrapper JAR is missing. Run 'gradle wrapper --gradle-version 8.7' to generate it." >&2
  exit 1
fi

exec "$JAVA_BIN" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
