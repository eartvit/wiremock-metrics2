#!/bin/bash

# For testing purposes
# Ensure you have a podman network called `test` and that has DNS enabled. You can create one with `podman network create test`
# Then build the app as container in your local store using `podman build -t wiremock-metrics2 -f Containerfile`.
# Once the build is completed you can use the launcher script.

podman rm wiremock-metrics2 > /dev/null 2>&1

podman run -d --name wiremock-metrics2 --net test -p 8080:8080 -p 9090:9090 \
            -e TRACEACTIVE='True' -e DELAYLOWERBOUNDMS=150 -e DELAYUPPERBOUNDMS=350 \
            -e DEEP_LEVEL='False' \
            -e WIREMOCKPORT=8080  -e PROMETHEUSPORT=9090 \
            -e CONTAINERTHREADS=125 -e ASYNCRESPENABLED='True' -e ASYNCRESPTHREADS=25 \
            -e JETTYACCEPTORS=110 -e JETTYACCEPTORSQSIZE=1000 localhost/wiremock-metrics2:latest
