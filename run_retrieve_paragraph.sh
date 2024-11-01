#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.aiutils.RetrieveParagraph" -Dexec.args="./src/main/resources/paragraph_store.json"
