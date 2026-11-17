# CareerBridge - container image (frontend + Java backend in one service)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY backend/src/main ./backend/src/main
RUN mkdir out && javac -encoding UTF-8 -d out $(find backend/src/main/java -name '*.java')

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home app && mkdir /data && chown app /data
COPY --from=build /app/out ./classes
COPY lib/h2.jar ./lib/h2.jar
COPY *.html *.css *.js ./
COPY assets ./assets
ENV PORT=8080 DATA_DIR=/data STATIC_DIR=/app
VOLUME /data
EXPOSE 8080
USER app
CMD ["java", "-cp", "classes:lib/h2.jar", "com.careerbridge.Main"]
