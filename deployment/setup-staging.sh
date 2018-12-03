#!/bin/sh
# Creates a fresh staging environment for testing

set -e

if [ $# -eq 0 ]
  then
    echo "Usage: ./deployment/setup-staging.sh [project-id]"
    echo
    echo "Where project-id is an ID for a Google Cloud Project that "
    echo "does not yet exist."
    exit 1
fi

# Change directory to project root

cd "$(dirname "$0")"
cd ..

PROJECT=$1

echo "Creating staging environment $PROJECT..."

# Create the project

gcloud projects create $PROJECT --organization=1003558959091 --set-as-default --enable-cloud-apis
gcloud alpha billing projects link $PROJECT --billing-account=00D8A9-A726AA-0E429C

# Create the AppEngine instance

gcloud app create --region=europe-west

# Create the MySQL database

gcloud sql instances create activityinfo \
    --activation-policy=on-demand \
    --database-version=MYSQL_5_5 \
    --follow-gae-app=$PROJECT \
    --authorized-gae-apps=$PROJECT \
    --region=europe-west1 \
    --tier=D2

gcloud sql databases create activityinfo --instance=activityinfo --charset=utf8mb4

# Create the initial database schema dump file
./gradlew setupTestDatabase

mysqldump aitest > /tmp/init.sql
cat store/mysql/geography.sql >> /tmp/init.sql
cat store/mysql/nullary-locations.sql >> /tmp/init.sql
cat store/mysql/training-db-stub.sql >> /tmp/init.sql
cat store/mysql/bound-location-types.sql >> /tmp/init.sql
gzip -f /tmp/init.sql

gsutil cp /tmp/init.sql.gz gs://staging.$PROJECT.appspot.com

gcloud --quiet sql import sql activityinfo gs://staging.$PROJECT.appspot.com/init.sql.gz \
    --database=activityinfo

# Create configuration file

cat > /tmp/config.properties <<- END_OF_CONFIG
hibernate.connection.driver_class=com.mysql.jdbc.GoogleDriver
hibernate.connection.url=jdbc:google:mysql://$PROJECT:activityinfo/activityinfo?useUnicode=true&characterEncoding=utf8&user=root&zeroDateTimeBehavior=convertToNull
END_OF_CONFIG

gsutil cp /tmp/config.properties gs://$PROJECT.appspot.com
