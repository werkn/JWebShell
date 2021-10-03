#!/bin/bash
#adapted from this tutorial:https://dzone.com/articles/securing-rest-apis-with-client-certificates

mkdir two-way-ssl && cd two-way-ssl && mkdir client && mkdir server

## Server
# Generate server private key and self-signed certificate in one step (we did this in two steps in one-way)

echo -e "\n\nCreating server keys and self-signed cert\n\n"

openssl req -x509 -newkey rsa:4096 -keyout server/serverPrivateKey.pem -out server/server.crt -days 3650 -nodes
# Create PKCS12 keystore containing private key and related self-sign certificate

echo -e "\n\nCreating PKCS12 keystore...\n\n"

openssl pkcs12 -export -out server/keyStore.p12 -inkey server/serverPrivateKey.pem -in server/server.crt
# Generate server trust store from server certificate 

echo -e "\n\nIMPORTING server cert to server trust store\n\n"

keytool -import -trustcacerts -alias root -file server/server.crt -keystore server/trustStore.jts

## Client
# Generate client's private key and a certificate signing request (CSR)

echo -e "\n\nGenerate client's private key and a certificate signing request (CSR)\n\n"

openssl req -new -newkey rsa:4096 -out client/request.csr -keyout client/myPrivateKey.pem -nodes

## Server

echo -e "\n\nSign client's CSR with server private key and a related certificate\n\n"

# Sign client's CSR with server private key and a related certificate
openssl x509 -req -days 360 -in client/request.csr -CA server/server.crt -CAkey server/serverPrivateKey.pem -CAcreateserial -out client/pavel.crt -sha256

echo -e "\n\nVerify client's certificate\n\n"

## Client
# Verify client's certificate
openssl x509 -text -noout -in client/pavel.crt

echo -e "\n\nCreate PKCS12 keystore containing client's private key and related self-sign certificate \n\n"

# Create PKCS12 keystore containing client's private key and related self-sign certificate 
openssl pkcs12 -export -out client/client_pavel.p12 -inkey client/myPrivateKey.pem -in client/pavel.crt -certfile server/server.crt

# Lastly, add the server to a trust store for the client
keytool -import -trustcacerts -alias root -file server/server.crt -keystore client/trustStore.jts