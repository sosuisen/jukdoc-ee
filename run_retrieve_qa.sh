#!/bin/bash
./mvnw exec:java -Dexec.mainClass="net.sosuisen.offlineutils.RetrieveQA" -Dexec.args="./src/main/resources/qa_store.json"
