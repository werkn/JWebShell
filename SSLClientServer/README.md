# SSLClientServer Setup Guide

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