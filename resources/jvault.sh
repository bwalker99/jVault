#!/bin/bash
# jVault on Unix
# TODO - need to be able to run by clicking script in file browser.

if [ ! -d "$JAVA_HOME" ] ; then
    JAVA=java
  else
    JAVA=${JAVA_HOME}/bin/java
fi

export CLASSPATH=lib/commons-codec-1.4.jar:lib/commons-lang-2.5.jar:lib/jasypt-1.6.jar:lib/jvault.jar
$JAVA ca/cogomov/jvault/jVault $1
