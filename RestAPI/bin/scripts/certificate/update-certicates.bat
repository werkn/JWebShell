::copy to server from our generated keys
copy /-Y ..\..\..\SSLGenerator\export\one-way-ssl\ssl-server-keystore.jks ..\..\src\test\resources\ssl-server-keystore.jks
copy /-Y ..\..\..\SSLGenerator\export\one-way-ssl\ssl-server-keystore.jks ..\..\src\main\resources\ssl-server-keystore.jks
