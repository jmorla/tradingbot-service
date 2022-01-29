FROM openjdk:11
COPY ./build/libs/tradingboot-1.0.RELEASE.jar ./app.jar

CMD ["java", "-jar", "app.jar"]