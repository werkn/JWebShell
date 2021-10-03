# JWebShell
JWebShell is a tool developed using Java 8, Spring Boot and MySQL to provide remote system shells.

# Overview

The application was used to teach myself Spring Boot and Java SSL network programming.

Architecture wise it consists of three major components:
 - A **RestAPI** that registers listening shell servers, and provides clients with access token to access listening servers
 - **SSL Clients** using JTS/JKS for two-way SSL authentication that connect to remote shell servers and spawn a command line based system shell
 - **SSL Shell Servers** using JTS/JKS for two-way SSL authentication that register themselves with the RestAPI and generate a unique access token to be provided by connecting clients

## Features:
 - MySQL, Docker, Multi-threading, SSL sockets, SSL certificate generation, X.509 certificates

# Setup

We begin by configuring Spring Boot RestAPI and setting up a JDK 1.8 environment.

## Setup RestAPI (Spring Boot)
---
To configure MySQL and the test database install Docker / Docker-Compose and run:

```bash
docker compose up -d
``` 

in the root of this directory.  Wait for the container to build before moving on to **Configure Eclipse Project**.

## Configure Eclipse Project

The RestAPI has a dependency on JDK 1.8 to configure this on your system (assuming your using Eclipse) perform the following:

1.  Download JDK 1.8 from [https://www.oracle.com/ca-en/java/technologies/javase/javase8-archive-downloads.html](https://www.oracle.com/ca-en/java/technologies/javase/javase8-archive-downloads.html).  Select the 64-bit version for your platform, download and install it.  Note the installation path we will need this shortly.

2.  Launch Eclipse and select `File->Open Projects From File System` locate the RestAPI folder in the JWebShell repository.  It will take a few minutes to import the Maven project.

3.  When importing has completed we will need to configure the `Installed JREs` to do this navigate to `Window->Preferences`.  Expand the `Java` tree and select `Installed JREs`.  

4.  Click `Add` on the right hand side of the menu.  Select `Standard VM` and click `Next`.  For JRE Home field click `Directory` and navigate to the location of JDK 1.8.  Click OK and the JRE system libraries should populate, click `Finish`.

5.  Lastly add `JAVA_HOME=PATH_TO_JDK1.8` to your environment variables.

6.  `JDK 1.8` should now be configured for use.  To test this right-click `Deploy (Windows).launch` and the RestAPI should launch and await connections. 


## Postman Sample Queries

Postman sample queries for the API are found in the root of the folder in file:

`./RestAPI.postman_collection.json`

You'll need to install the generated self-signed certs in `./SSLGenerator/export` or disable SSL verification in Postman.

## Setup SSL Clients / SSL Shell Servers
---
## Setup Docker Container

Before running and configuring SSLClientServer you must configure the RestAPI.  Follow these instructions fist if you have not completed the RestAPI setup.

## Configure Eclipse Project

**Note**:  You should have already configured your system to run JDK 1.8 and have the RestAPI configured and running.  

The SSLClientServer has a dependency on JDK 1.8 to configure this on your system (assuming your using Eclipse) perform the following:

1.  Launch Eclipse and select `File->Open Projects From File System` locate the SSLClientServer folder in the JWebShell repository.  It will take a few minutes to import the project.

2.  After the project loads expand folder `SSLClientServer` and `Right-click SSLShellServer.launch -> Run as SSLShellServer` and wait for setup to complete/registration to RestAPI to complete.

3.  Finally launch `Right-click SSLConsoleClient-> Run as SSLConsoleClient` wait for client to start, it will provide a list of servers from the rest API as follows:

```bash
(0) -> 127.0.0.1
(1) -> 10.0.0.25
...
```

Enter the index (0,1...,etc) of the server you wish to connect to and press `Enter`.  The client will request a remote shell.  If everything is successful you should see the following in the console:

```powershell
(0) -> 127.0.0.1:4444
0
Spawning a shell....
AccessToken is valid.  Waiting for commands...
Windows PowerShell
Copyright (C) Microsoft Corporation. All rights reserved.

Try the new cross-platform PowerShell https://aka.ms/pscore6
>
```
You should now be able to send remote shell commands to the shell server.  When your finished enter `exit_session` and the SSL connection/shell session will be shutdown gracefully.  If you send `SIGINT` or exit the shell without `exit_session` verbose logging is enabled so you may see a SOCKET exception in your consoles STDERR. 


