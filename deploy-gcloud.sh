#!/bin/bash
# Qayta deploy qilish uchun skript
# Ishlatish: bash deploy-gcloud.sh

set -e

PROJECT_ID="queuemanagementsystem-495505"
REGION="us-central1"
REPO_NAME="qms-repo"
SERVICE_NAME="qms-app"
IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/${SERVICE_NAME}"
DB_URL="jdbc:postgresql://34.122.183.117:5432/qms"

echo "=== Build ==="
gcloud builds submit --tag ${IMAGE}:latest .

echo "=== Deploy ==="
gcloud run deploy $SERVICE_NAME \
  --image=${IMAGE}:latest \
  --region=$REGION \
  --platform=managed \
  --allow-unauthenticated \
  --port=9092 \
  --memory=512Mi \
  --set-env-vars="DB_URL=${DB_URL},DB_USERNAME=postgres" \
  --set-secrets="DB_PASSWORD=qms-db-password:latest,JWT_SECRET=qms-jwt-secret:latest"

echo ""
echo "=== DEPLOY TUGADI ==="
gcloud run services describe $SERVICE_NAME --region=$REGION --format='value(status.url)'
