FROM payara/micro:6.2024.10-jdk21
COPY target/jukdoc-ee-0.1-SNAPSHOT.war $DEPLOY_DIR
CMD ["deployments/jukdoc-ee-0.1-SNAPSHOT.war", "--contextroot", "/", "--nocluster"]
EXPOSE 8080
