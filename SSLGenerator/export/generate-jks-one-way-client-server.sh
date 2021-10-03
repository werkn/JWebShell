#!/bin/bash

echo "This script will generate a client/server configuration for one-way SSL.  It will generate a "
echo "three files, ssl-server-keystore.jks which contains a single key (Private/Public) pair. We then "
echo "export a certificate for this key to file ssl-client.cert.  Lastly we import this into "
echo "ssl-client-trust-store.jts."

mkdir one-way-ssl
cd one-way-ssl

#remove old keys
rm ssl-server-keystore.jks
rm ssl-client.cert
rm ssl-client-trust-store.jts

#first we need to generate public/private keys for the Server
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -alias ssl-server-key -keystore ssl-server-keystore.jks

#at this point we have a public/private key pair but still need to export the certificate(public key of Server), we do the following
keytool -export -alias ssl-server-key -keystore ssl-server-keystore.jks -file ssl-client.cert

#now we need to import our exported certificate into a truststore which (for our purposes) can be viewed as a list of 
#server certificates that we implicitly trust
keytool -import -file ssl-client.cert -alias ssl-client-cert -keystore ssl-client-trust-store.jts
