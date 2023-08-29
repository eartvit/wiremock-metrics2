FROM registry.access.redhat.com/ubi8/openjdk-17-runtime

COPY ./target/wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar

# Define ENV defaults
# Turn off app tracing
ENV TRACEACTIVE="False"
# Turn on/off secondary, deep level request
ENV DEEP_LEVEL="False"
# Deep level endpoint. DO NOT POINT TO SELF, THE REQUEST WILL BE IN AN ENDLESS LOOP!!!
ENV DEEP_ENDPOINT="http://wiremock2.demo.apps.svc.local:8080/mock"
# Lower bound delay ms for response (uniform distribution)
ENV DELAYLOWERBOUNDMS=100
# Upper bound delay ms for response (uniform distribution)
ENV DELAYUPPERBOUNDMS=300
# Wiremock port
ENV WIREMOCKPORT=8080
# Prometheus port
ENV PROMETHEUSPORT=9090
# Wiremock Jetty server configuration parameters
ENV CONTAINERTHREADS=125
ENV ASYNCRESPENABLED="True"
ENV ASYNCRESPTHREADS=25
#Jetty Acceptor threads must be at most CONTAINERTHREADS-10
ENV JETTYACCEPTORS=110
ENV JETTYACCEPTORSQSIZE=1000

CMD java -jar wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar
