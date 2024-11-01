#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.aiutils.RetrieveQA" -Dexec.args="./src/main/resources/qa_store.json"
