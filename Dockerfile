FROM openjdk:17
COPY build/libs/MoneyBunny-backend-1.0-SNAPSHOT.war app.war
CMD ["java", "-jar", "app.war"]
