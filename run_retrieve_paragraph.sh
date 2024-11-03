#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.offlineutils.RetrieveParagraph" -Dexec.args="./src/main/resources/paragraph_store.json"
