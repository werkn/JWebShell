FROM ubuntu:latest

RUN apt-get -y update --fix-missing
RUN apt-get upgrade -y

#get openssl 
RUN apt-get install openssl -y

#get a JRE so we can access keytool
RUN apt-get install default-jre -y

#get vim for editing (opt)
RUN apt-get install vim -y
