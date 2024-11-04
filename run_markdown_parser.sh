#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <path-to-markdown-file>"
  echo "Example: $0 /home/sales/docs/sales_en.md"
  exit 1
fi

ARGS="$1"

./mvnw exec:java -Dexec.mainClass="net.sosuisen.offlineutils.MarkdownParser" -Dexec.args="$ARGS ./src/main/resources/structured_paragraph.txt"
