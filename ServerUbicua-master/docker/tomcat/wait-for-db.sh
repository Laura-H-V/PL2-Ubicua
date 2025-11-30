#!/bin/sh
DB_HOST=${DB_HOST:-db}
DB_PORT=${DB_PORT:-5432}
RETRIES=${RETRIES:-60}
COUNT=0
echo "Waiting for database $DB_HOST:$DB_PORT..."
while ! nc -z "$DB_HOST" "$DB_PORT"; do
  COUNT=$((COUNT+1))
  echo "DB not reachable yet ($COUNT/$RETRIES) - sleeping 1s"
  if [ "$COUNT" -ge "$RETRIES" ]; then
    echo "Timeout waiting for database after $RETRIES attempts"
    exit 1
  fi
  sleep 1
done
echo "Database reachable, starting Tomcat"
exec catalina.sh run
