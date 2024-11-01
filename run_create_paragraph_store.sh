#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.aiutils.CreateParagraphStore" -Dexec.args="./src/main/resources/structured_paragraph.txt"
