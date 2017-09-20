#!/usr/bin/env bash

set -e

#this script generates a bunch certificates that are used for testing, the generated certificates are already committed to repo
keytool -genkeypair -alias secure-server -keyalg RSA -dname "CN=localhost,OU=myorg,O=myorg,L=mycity,S=mystate,C=es" -keypass secret -keystore server-keystore.jks -storepass secret -validity 3650
keytool -genkeypair -alias secure-client -keyalg RSA -dname "CN=codependent-client,OU=myorg,O=myorg,L=mycity,S=mystate,C=es" -keypass secret -keystore client-keystore.jks -storepass secret -validity 3650

keytool -exportcert -alias secure-client -file client-public.cer -keystore client-keystore.jks -storepass secret -validity 3650
keytool -importcert -keystore server-truststore.jks -alias clientcert -file client-public.cer -storepass secret -validity 3650

keytool -exportcert -alias secure-server -file server-public.cer -keystore server-keystore.jks -storepass secret -validity 3650
keytool -importcert -keystore client-truststore.jks -alias servercert -file server-public.cer -storepass secret -validity 3650

keytool -v -importkeystore -srckeystore client-keystore.jks  -srcalias secure-client -destkeystore myp12file.p12 -deststoretype PKCS12 -deststorepass secret -srcstorepass secret
openssl pkcs12 -in myp12file.p12 -clcerts -nokeys -out client.crt
openssl pkcs12 -nocerts -in myp12file.p12 -out client.key -nodes
