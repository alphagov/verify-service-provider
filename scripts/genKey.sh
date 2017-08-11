#!/usr/bin/env bash

export name="${1:?Usage $0 name commonName}"
export commonName="${2:?Usage $0 name commonName}"

# Generate a private key:
openssl genrsa -des3 -passout pass:x -out "$name.pass.key" 2048
openssl rsa -passin pass:x -in "$name.pass.key" -out "$name.key"

# Generate a certificate signing request (CSR):
openssl req -batch -new -subj "/CN=$commonName" -key "$name.key" -out "$name.csr"

# Generate a self signed certificate:
openssl x509 -req -sha256 -in "$name.csr" -days 36500 -signkey "$name.key" -out "$name.crt"

# Convert the private key to .pk8 format:
openssl pkcs8 -topk8 -inform PEM -outform DER -in "$name.key" -out "$name.pk8" -nocrypt

# Clean up the files you don’t need anymore:
rm "$name.pass.key"
rm "$name.csr"
rm "$name.key"
