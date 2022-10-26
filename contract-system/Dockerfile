FROM openjdk:11

ADD  /target/contract-system-2.6.jar /app.jar
ADD  /jrebel.jar /jrebel.jar
ADD  /libjrebel64.so /libjrebel64.so
ENTRYPOINT ["java",  "-jar", "-Xdebug", "-Xrunjdwp:server=y,transport=dt_socket,address=9000,suspend=n","-agentpath:/libjrebel64.so","-Drebel.remoting_plugin=true","-Djava.security.egd=file:/dev/./urandom", "/app.jar"]