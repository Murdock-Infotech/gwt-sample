#!/bin/sh

# Resolve this script's directory to an absolute path (works even when invoked from elsewhere).
HOMEDIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

# If this script was copied out of the GWT distribution dir, the jars won't be alongside it anymore.
# Prefer jars next to this script (original behavior), otherwise fall back to the repo layout.
if [ -f "$HOMEDIR/gwt-codeserver.jar" ]; then
  GWT_DIR="$HOMEDIR"
else
  GWT_DIR="$HOMEDIR/lib/gwt-2.12.2"
fi

# Optional GWT-provided extras (present in the full GWT distribution).
VALIDATION_API_JAR="$GWT_DIR/validation-api-1.0.0.GA.jar"
VALIDATION_API_SOURCES_JAR="$GWT_DIR/validation-api-1.0.0.GA-sources.jar"
EXTRA_GWT_JARS=""
if [ -f "$VALIDATION_API_JAR" ]; then
  EXTRA_GWT_JARS="$EXTRA_GWT_JARS:$VALIDATION_API_JAR"
fi
if [ -f "$VALIDATION_API_SOURCES_JAR" ]; then
  EXTRA_GWT_JARS="$EXTRA_GWT_JARS:$VALIDATION_API_SOURCES_JAR"
fi

# Add project sources to the classpath so CodeServer can see .java and .gwt.xml sources.
CLIENT_SRC="$HOMEDIR/src/main/java"
SHARED_SRC="$HOMEDIR/../modular-webapp-shared/src/main/java"
CLIENT_CLASSES="$HOMEDIR/target/classes"
SHARED_CLASSES="$HOMEDIR/../modular-webapp-shared/target/classes"

EXTRA_CP=""
if [ -d "$CLIENT_SRC" ]; then
  EXTRA_CP="$EXTRA_CP:$CLIENT_SRC"
fi
if [ -d "$SHARED_SRC" ]; then
  EXTRA_CP="$EXTRA_CP:$SHARED_SRC"
fi
if [ -d "$CLIENT_CLASSES" ]; then
  EXTRA_CP="$EXTRA_CP:$CLIENT_CLASSES"
fi
if [ -d "$SHARED_CLASSES" ]; then
  EXTRA_CP="$EXTRA_CP:$SHARED_CLASSES"
fi

# Optional debug output: run with DEBUG_CP=1 to print the extra classpath entries.
if [ "${DEBUG_CP:-0}" != "0" ]; then
  echo "$EXTRA_CP"
fi
java -cp "$GWT_DIR/gwt-codeserver.jar:$GWT_DIR/gwt-user.jar:$GWT_DIR/gwt-dev.jar$EXTRA_GWT_JARS$EXTRA_CP" \
  com.google.gwt.dev.DevMode -noincremental -startupUrl http://localhost:8080 "$@"
