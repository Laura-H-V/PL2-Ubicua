# ServerUbicua - Docker infrastructure

This project is the base for the Ubicua server that provides an HTTP endpoint (Tomcat), a PostgreSQL database, and an MQTT broker.

What I added for the Docker assignment:

- `Docker_compose.yml`: Compose file to launch the three services (PostgreSQL, Mosquitto, Tomcat). It configures restart policies and an internal network.
- `docker/postgres/init/init.sql`: SQL script that creates the `UBICOMP` database, the `MEASUREMENT` table and a sample user `ubicomp_user` (password `ubicomp_pass`) with initial data. This runs automatically on first DB initialization.
- `docker/mosquitto/config/mosquitto.conf`: Basic Mosquitto configuration with persistence and listener on `1883`.
- `docker/tomcat/Dockerfile`: Dockerfile that installs the MariaDB JDBC driver, copies a `context.xml` (JNDI DataSource) and a pre-built WAR `webapps/ROOT.war` if you provide it.
- `docker/tomcat/context.xml`: Tomcat Context with a JNDI `jdbc/ubicomp` DataSource (connection pool).

Important notes and how to use

1. Build the webapp WAR locally (recommended). From the project root run:

```powershell
mvn -f pom.xml clean package
```

2. Copy the WAR into `docker/tomcat/webapps/ROOT.war` (or place your exploded app in that directory). See `docker/tomcat/webapps/README.txt`.

3. Launch the stack (from project root):

```powershell
docker compose -f Docker_compose.yml up -d --build
```

4. Services and ports:
- MQTT: `1883` (published to host)
-- Tomcat: `8081` (published to host, maps to container `8080`)
-- PostgreSQL: NOT published to the host (accessible only from the Docker network)

5. Tomcat JNDI DataSource

The Tomcat `context.xml` creates a JNDI DataSource `jdbc/ubicomp` pointing to the `db` service. The application code looks up `java:/comp/env/jdbc/ubicomp` (see `src/main/java/Database/ConectionDDBB.java`). If your application expects a different JNDI name, update `docker/tomcat/context.xml` accordingly.

6. Persistence

All services have mounted volumes so data persists between container restarts.

If you want, I can:
- Adjust the Tomcat image to build the WAR inside the Docker build (requires a JDK/Maven image supporting Java 24), or
- Change MQTT exposure policy (if you prefer MQTT not exposed to host), or
- Add healthchecks and more secure credentials.
