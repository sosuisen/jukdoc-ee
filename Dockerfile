FROM payara/micro:6.2024.10-jdk21
COPY target/jukdoc-0.1-SNAPSHOT.war $DEPLOY_DIR
