FROM openjdk:17-slim
WORKDIR /app
COPY . .
RUN javac jukebox_cli/src/jukebox_cli/*.java -d jukebox_cli/bin
CMD ["java", "-cp", "jukebox_cli/bin:jukebox_cli/src", "jukebox_cli.JukeboxHttpServer"]