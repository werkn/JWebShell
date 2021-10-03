# RestAPI Setup Guide

## Setup Docker Container

To configure MySQL and the test database install Docker / Docker-Compose and run:

```bash
docker compose up -d
``` 

in the root of this directory.  Wait for the container to build before moving on the **Configure Eclipse Project**.

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