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

# Configuration
ORGANIZATION=1003558959091
BILLING=00D8A9-A726AA-0E429C
JENKINS=135288259907-7v3lrtkjp1fs6anotbo3e9ebcq3jto32@developer.gserviceaccount.com


# Create the project

gcloud projects create $PROJECT --organization=$ORGANIZATION --set-as-default --enable-cloud-apis
gcloud alpha billing projects link $PROJECT --billing-account=$BILLING

# Create the AppEngine instance

gcloud app create --region=europe-west

# Grant Jenkins the permissions to deploy apps here

gcloud services enable appengine.googleapis.com

gcloud projects add-iam-policy-binding $PROJECT \
    --member serviceAccount:$JENKINS \
    --role roles/appengine.appAdmin \
    --role roles/datastore.indexAdmin \
    --role roles/cloudsql.admin \
    --role roles/cloudtasks.queueAdmin

# Create the MySQL database

gcloud sql instances create activityinfo \
    --activation-policy=on-demand \
    --database-version=MYSQL_5_5 \
    --follow-gae-app=$PROJECT \
    --authorized-gae-apps=$PROJECT \
    --region=europe-west1 \
    --assign-ip \
    --authorized-networks=0.0.0.0/0 \
    --require-ssl \
    --tier=D2

gcloud sql databases create activityinfo --instance=activityinfo --charset=utf8mb4

# Create a login we can use to run liquibase
gcloud sql users create root --instance=activityinfo --host="%" --password=root


# Create the initial database schema dump file
./gradlew setupTestDatabase

mysqldump aitest > /tmp/init.sql
cat store/mysql/geography.sql >> /tmp/init.sql
cat store/mysql/nullary-locations.sql >> /tmp/init.sql
cat store/mysql/training-db-stub.sql >> /tmp/init.sql
cat store/mysql/bound-location-types.sql >> /tmp/init.sql
cat store/mysql/dev-user.sql >> /tmp/init.sql
gzip -f /tmp/init.sql

gsutil cp /tmp/init.sql.gz gs://staging.$PROJECT.appspot.com

gcloud --quiet sql import sql activityinfo gs://staging.$PROJECT.appspot.com/init.sql.gz \
    --database=activityinfo

# Create a bucket for attachment fields
ATTACHMENT_BUCKET=$PROJECT-attachments
gsutil mb -p $PROJECT -l europe-west4 gs://$ATTACHMENT_BUCKET

# Create a bucket for temporary generated files
GENERATED_BUCKET=$PROJECT-generated
gsutil mb -p $PROJECT -l eu --retention 14d gs://$GENERATED_BUCKET


# Create configuration file

cat > /tmp/config.properties <<- END_OF_CONFIG
hibernate.connection.driver_class=com.mysql.jdbc.GoogleDriver
hibernate.connection.url=jdbc:google:mysql://$PROJECT:activityinfo/activityinfo?useUnicode=true&characterEncoding=utf8&user=root&zeroDateTimeBehavior=convertToNull
blobservice.gcs.bucket.name=$ATTACHMENT_BUCKET
generated.resources.bucket=$GENERATED_BUCKET
END_OF_CONFIG

gsutil cat gs://ai-config/postmark.properties >> /tmp/config.properties
gsutil cat gs://ai-config/mailchimp.properties >> /tmp/config.properties

gsutil cp /tmp/config.properties gs://$PROJECT.appspot.com

rm /tmp/config.properties

# Deploy marketing site

# (TODO)

# Update dispatch rules

gcloud --quiet app deploy ./deployment/dispatch.yaml