#!/bin/bash
./node_exporter &
java -jar wiremock-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar


