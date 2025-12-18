# Utilise l'image officielle WildFly avec JDK 17
FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk17

# Copie le jar EJB dans le dossier de déploiement WildFly
COPY ejb/target/configservice-1.0-SNAPSHOT.jar /opt/jboss/wildfly/standalone/deployments/configservice.jar

# Optionnel : copie d'autres fichiers si besoin
# COPY config/ /opt/jboss/wildfly/standalone/config/

# Télécharge le driver PostgreSQL
RUN cd /opt/jboss/wildfly/standalone/deployments && \
    curl -O https://jdbc.postgresql.org/download/postgresql-42.2.27.jar && \
    cd /opt/jboss/wildfly/modules && \
    mkdir -p org/postgresql/main && \
    cp /opt/jboss/wildfly/standalone/deployments/postgresql-42.2.27.jar org/postgresql/main/ && \
    echo "module add --name=org.postgresql --resources=org/postgresql/main/postgresql-42.2.27.jar --dependencies=javax.api,javax.transaction.api" > /tmp/add_pg_module.cli && \
    /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/add_pg_module.cli || true

# Ajoute le script CLI pour la datasource
COPY wildfly-datasource.cli /opt/jboss/wildfly-datasource.cli

# Applique la datasource au build
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly-datasource.cli

# Démarre simplement WildFly
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]

EXPOSE 8080