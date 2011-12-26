#!/bin/sh

mvn -q -f luhn-algorithm/pom.xml clean compile exec:java -Dexec.mainClass=mahergamal.logging.filters.LuhnLogFilter -Dexec.arguments="$*"
