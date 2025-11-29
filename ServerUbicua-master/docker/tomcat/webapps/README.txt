Place the built WAR here as `ROOT.war` before building the Tomcat image.

How to produce the WAR locally:

  mvn -f pom.xml clean package

Then copy the generated WAR from `target/ServerExampleUbicomp-1.0-SNAPSHOT.war` or the exploded
folder `target/ServerExampleUbicomp-1.0-SNAPSHOT` into this `webapps/ROOT.war`.
