#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.offlineutils.CreateQAStore" -Dexec.args="./src/main/resources/structured_paragraph.txt"
