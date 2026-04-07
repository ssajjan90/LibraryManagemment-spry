#!/usr/bin/env bash
set -euo pipefail

# Usage:
# AWS_REGION=us-east-1 AWS_ACCOUNT_ID=123456789012 ECR_REPOSITORY=library-management IMAGE_TAG=1.0.0 ./scripts/push-ecr.sh

: "${AWS_REGION:?AWS_REGION is required}"
: "${AWS_ACCOUNT_ID:?AWS_ACCOUNT_ID is required}"
: "${ECR_REPOSITORY:?ECR_REPOSITORY is required}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE_URI="${REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}"

echo "Logging in to ECR: ${REGISTRY}"
aws ecr get-login-password --region "${AWS_REGION}" |
  docker login --username AWS --password-stdin "${REGISTRY}"

if ! aws ecr describe-repositories --repository-names "${ECR_REPOSITORY}" --region "${AWS_REGION}" >/dev/null 2>&1; then
  echo "Repository ${ECR_REPOSITORY} does not exist, creating..."
  aws ecr create-repository --repository-name "${ECR_REPOSITORY}" --region "${AWS_REGION}" >/dev/null
fi

echo "Building image ${IMAGE_URI}"
docker build -t "${IMAGE_URI}" .

echo "Pushing image ${IMAGE_URI}"
docker push "${IMAGE_URI}"

echo "Done. Image pushed: ${IMAGE_URI}"
