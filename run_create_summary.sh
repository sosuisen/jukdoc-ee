#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.offlineutils.CreateSummary" -Dexec.args="./src/main/resources/structured_paragraph.txt"
