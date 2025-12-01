FROM tomcat:10.1-jdk11


# Copy Tomcat context configuration (defines a JNDI DataSource)
COPY context.xml /usr/local/tomcat/conf/context.xml

# Copy pre-built WAR if provided by the developer into webapps/ROOT.war
COPY webapps/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Copy wait-for-db script
COPY wait-for-db.sh /wait-for-db.sh

# Install curl, netcat and add PostgreSQL JDBC driver to Tomcat lib
RUN apt-get update && apt-get install -y curl netcat-openbsd && \
    curl -L -o /usr/local/tomcat/lib/postgresql.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar && \
    rm -rf /var/lib/apt/lists/* && \
    chmod +x /wait-for-db.sh

EXPOSE 8080

# Start script will wait for DB then exec Tomcat
ENTRYPOINT ["/wait-for-db.sh"]
