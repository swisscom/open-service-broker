#!/bin/bash
set -e
wget https://dl.minio.io/client/mc/release/linux-amd64/mc
chmod +x mc
./mc -C .mc mb s3/backup --insecure
