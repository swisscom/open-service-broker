#!/bin/bash
set -x
mkdir -p vaultstorage s3data minio_config/certs
./generate_cert -ca --host "minio"
mv cert.pem minio_config/certs/public.crt
mv key.pem minio_config/certs/private.key
docker-compose up -d
./setup_minio.sh
./setup_shield.sh
