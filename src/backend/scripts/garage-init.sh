#!/bin/sh
set -e

S3_KEY="GKafc12d36554df7f8d5c4b7ba"
S3_SECRET="abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"

apk add --no-cache docker-cli python3 py3-pip > /dev/null 2>&1
pip3 install --break-system-packages boto3 > /dev/null 2>&1

# ===== FIND GARAGE CONTAINER =====
CONTAINER=$(docker ps --filter name=backend-garage-1 --format '{{.Names}}')
if [ -z "$CONTAINER" ]; then
  CONTAINER=$(docker ps --filter name=garage --filter ancestor=dxflrs/garage:v1.0.1 --format '{{.Names}}' | head -1)
fi

if [ -z "$CONTAINER" ]; then
  echo "❌ Garage container not found"
  exit 1
fi
echo "Using container: $CONTAINER"

# ===== LAYOUT =====
NODE_ID=$(docker exec "$CONTAINER" /garage -c /etc/garage.toml status 2>/dev/null | grep 'NO ROLE ASSIGNED' | awk '{print $1}')
if [ -n "$NODE_ID" ]; then
  echo "Assigning layout to node $NODE_ID"
  docker exec "$CONTAINER" /garage -c /etc/garage.toml layout assign -z dc1 -c 10G "$NODE_ID"
  docker exec "$CONTAINER" /garage -c /etc/garage.toml layout apply --version 1
  sleep 2
fi

# ===== KEY =====
docker exec "$CONTAINER" /garage -c /etc/garage.toml key import -n app-files-key --yes "$S3_KEY" "$S3_SECRET" 2>/dev/null || true

# ===== BUCKET =====
docker exec "$CONTAINER" /garage -c /etc/garage.toml bucket create files 2>/dev/null || true
docker exec "$CONTAINER" /garage -c /etc/garage.toml bucket allow --read --write --owner files --key "$S3_KEY"

echo "✅ Garage bucket + key configured"

# ===== CORS (via boto3) =====
python3 /configure-cors.py

echo "✅ Garage initialization complete"
