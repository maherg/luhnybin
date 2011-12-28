#!/bin/sh

mvn -q -f creditcard-filter/pom.xml clean compile exec:java -Dexec.mainClass=mahergamal.logging.filters.ChecksumBasedCreditCardFilter
