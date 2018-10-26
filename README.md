# SlashBackend

This Backend needs an HTTPS certificate in order to run. 

Tomcat may need additional APR binaries because Java 11 removed JavaEE which included the cryprographic libraries.
https://tomcat.apache.org/download-native.cgi


The main entry point is /slash in which a web ui is configured to directly test the slash Backend.