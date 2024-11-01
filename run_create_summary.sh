#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.aiutils.CreateSummary" -Dexec.args="./src/main/resources/structured_paragraph.txt"
