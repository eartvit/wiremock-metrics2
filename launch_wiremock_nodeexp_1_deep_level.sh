#!/bin/bash

# For testing purposes
# Ensure you have a podman network called `test` and that has DNS enabled. You can create one with `podman network create test`
# Then build the app as container in your local store using `podman build -t wiremock-metrics-nodeexporter -f Containerfile.node_exporter`.
# Once the build is completed you can use the launcher script.

podman rm --force wiremock-metrics-nodeexporter > /dev/null 2>&1
podman rm --force wiremock-metrics-nodeexporter-deep1 > /dev/null 2>&1

podman run -d --name wiremock-metrics-nodeexporter --net test -p 8080:8080 -p 9090:9090 -p 9100:9100 \
            -e TRACEACTIVE='True' -e DELAYLOWERBOUNDMS=150 -e DELAYUPPERBOUNDMS=350 \
            -e DEEP_LEVEL=1 \
            -e DEEP_ENDPOINT='http://wiremock-metrics-nodeexporter-deep1:8080/mock' \
            -e WIREMOCKPORT=8080 -e PROMETHEUSPORT=9090 \
            -e CONTAINERTHREADS=125 -e ASYNCRESPENABLED='True' -e ASYNCRESPTHREADS=25 \
            -e JETTYACCEPTORS=110 -e JETTYACCEPTORSQSIZE=1000 localhost/wiremock-metrics2:latest

podman run -d --name wiremock-metrics-nodeexporter-deep1 --net test -p 8081:8080 -p 9091:9090 -p 9101:9100 \
            -e TRACEACTIVE='True' -e DELAYLOWERBOUNDMS=150 -e DELAYUPPERBOUNDMS=350 \
            -e DEEP_LEVEL=0 \
            -e WIREMOCKPORT=8080  -e PROMETHEUSPORT=9090 \
            -e CONTAINERTHREADS=125 -e ASYNCRESPENABLED='True' -e ASYNCRESPTHREADS=25 \
            -e JETTYACCEPTORS=110 -e JETTYACCEPTORSQSIZE=1000 localhost/wiremock-metrics-nodeexporter:latest 

# Increase the level of depth as needed by launching other wiremock-metrics-nodeexporter instances and update their name and DEEP_ENDPOINT values as needed.