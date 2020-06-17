# Gerar o JAR
FROM clojure:openjdk-11-tools-deps AS build-env

WORKDIR /build

COPY deps.edn ./
COPY build build
COPY resources resources
COPY src src

# RUN clojure -R:build -e 'nil'
RUN clojure -A:build

# Start App
FROM openjdk:11.0.2-jdk-oraclelinux7 as app-env

WORKDIR /var/app

COPY --from=build-env /build/target/lib/lib lib
COPY --from=build-env /build/target/classes classes
COPY --from=build-env /build/resources resources

ENV DB_HOST=""
ENV DB_PORT=5234

EXPOSE 80

CMD java -cp "classes:lib/*:resources" nlw.main
