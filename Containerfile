FROM registry.access.redhat.com/ubi8/openjdk-17-runtime

COPY ./target/wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar

# Define ENV defaults
# Turn off app tracing
#ENV TRACEACTIVE="False"

# Turn on/off deep level request - to be used with outside scripting to go to the depth level defined by this variable
# In other words, if DEEP_LEVEL > 0 this instance will execute a deep call to the DEEP_ENDPOINT. The DEEP_LEVEL is reported for tracking purposes.
#ENV DEEP_LEVEL=0

# Deep level endpoint. DO NOT POINT TO SELF, THE REQUEST WILL BE IN AN ENDLESS LOOP!!!
# If DEEP_LEVEL > 0 then use some pattern to deploy multiple instance and construct this endpoint accordingly.
#ENV DEEP_ENDPOINT="http://wiremock-2.demo.apps.svc.local:8080/mock"

# Factor for deep level sent payload vs expected response payload
# The payload for the DEEP_LEVEL request is divided by the factor of the received input 
# E.g., the input request is of type /mock?id=100, then the size of the DEEP payload is 100/10 = 10
#ENV REQPAYLOADSIZEFACTOR=10

# Lower bound delay ms for response (uniform distribution)
#ENV DELAYLOWERBOUNDMS=100

# Upper bound delay ms for response (uniform distribution)
#ENV DELAYUPPERBOUNDMS=300

# Wiremock port
#ENV WIREMOCKPORT=8080

# Prometheus port
#ENV PROMETHEUSPORT=9090

# Wiremock Jetty server configuration parameters
#ENV CONTAINERTHREADS=125
#ENV ASYNCRESPENABLED="True"
#ENV ASYNCRESPTHREADS=25

#Jetty Acceptor threads must be at most CONTAINERTHREADS-10
#ENV JETTYACCEPTORS=110
#ENV JETTYACCEPTORSQSIZE=1000

ENV TRACEACTIVE="False" \
    DEEP_LEVEL=0 \
    DEEP_ENDPOINT="http://wiremock-2.demo.apps.svc.local:8080/mock" \
    REQPAYLOADSIZEFACTOR=10 \
    DELAYLOWERBOUNDMS=100 \
    DELAYUPPERBOUNDMS=300 \
    WIREMOCKPORT=8080 \
    PROMETHEUSPORT=9090 \
    CONTAINERTHREADS=125 \
    ASYNCRESPENABLED="True" \
    ASYNCRESPTHREADS=25 \
    JETTYACCEPTORS=110 \
    JETTYACCEPTORSQSIZE=1000

CMD java -jar wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar
