# SSLCertificateGenerator 

SSLCertificateGenerator  can be used to create self-signed *X.509 certificates*, JKS (Java Key Stores) and lastly JTS (Java Trust Stores) for use with Spring Boot/Java SSL.

For all of the instructions below were assuming your testing locally, meaning `localhost` will be your hostname.

## Generating One-Way Certificate (Typical)

1.  To generate a one-way certificate in the root of this directory run:

```bash
docker compose up -d
```

This will create our Docker container for generating certificates (it's pre-configured with a `JVM` and `openssl` among other tools).

2.  Next, attach to the container by running 

```bash
docker exec -it sslgenerator bash
```

3.  Inside the container run the following:

```bash
cd /export
bash generate-jks-one-way-client-server.sh
```

4.  Fill in all the prompts, be sure to specify `localhost` as your organization name / canonical name.  

5.  When everything is complete your certificates should be found in `./export/one-way-ssl` and the following files will be present:
 - `ssl-client-trust-store.jts` (For Java SSL clients)
 - `ssl-client.cert` (X.509 certificate)
 - `ssl-server-keystore.jks` (For the Spring Boot server)

### Importing To Spring Boot:
1.  Copy `./export/ssl-server-keystore.jks` to your Maven projects `src/main/resources` folder.
2.  Append the following to your apps, `application.properties`:
```bash
# SSL
server.port=8443
server.ssl.key-store=classpath:ssl-server-keystore.jks
server.ssl.key-store-password=123456

# JKS or PKCS12
server.ssl.keyStoreType=JKS

# Spring Security
security.require-ssl=true
```

## Generating Two-Way Certificate

If you want to generate a two-way X.509 certificate under an authentication scheme where the server validates to a client, and the client also validates themselves to the server complete the steps above but run `generate-jks-two-way-client-server.sh` for Step 3.