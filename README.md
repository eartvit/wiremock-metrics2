# wiremock-metrics2
This repository is a customized WireMock extended with Prometheus Java client integration and a global random string payload ResponseTransformer.

It services two POST requests: /mock and /mock?id=[integer], where the [integer] number is the expected response payload size (the random string).

The following Wiremock (and Jetty) configuration options are supported, and they must be provided as environment variables:
* WIREMOCKPORT: the port for the WireMock (Jetty) service.
* CONTAINERTHREADS: Jetty container threads.
* JETTYACCEPTORS: number of Jetty acceptor threads.
* JETTYACCEPTORSQSIZE: size of the Jetty queue.
* ASYNCRESPENABLED: Jetty asynchronous response control.
* ASYNCRESPTHREADS: Number of Jetty asynchronous response threads, provided that asynchronous response is enabled.

Additionally, the following variables are available, and must be provided:
* PROMETHEUSPORT: The port for the Prometheus service
* DELAYLOWERBOUNDMS: The lower bound in milliseconds for the response delay. A uniform distribution response delay strategy is implemented with the ResponseTransformer.
* DELAYUPPERBOUNDMS: The uppoer bound in milliseconds for the response delay. A uniform distribution response delay strategy is implemented with the ResponseTransformer.
* TRACEACTIVE: True/False, to enable/disable logs.

It may also initiate additional backend requests based on several control variables (defined as environment variables)
* DEEP_LEVEL: If 0 then it does not initiate any additional backend requests before responding to the original request. Any other value will trigger a new HTTP POST request having a randomly generated string as payload.
* REQPAYLOADSIZEFACTOR: The ratio between the incoming request 'id' integer value (used to generate the response payload) and the subsequent backend request payload length. Example if id=100 and REQPAYLOADSIZEFACTOR=10, then the response payload length for the original incoming request mocked by Wiremock shall be 100 and the payload length of the deep level request generated by WireMock before returning the response payload shall be 10.
* DEEP_ENDPOINT: The URL where to send the deep level request.

The repository contains some example launcher scripts that are based on Containerized version of the code stored in [quay.io](https://quay.io/avitui/repository/wiremock-metrics).

Using scripting, it is possible to build (automated) multi-level stacked deployments of the application, however it is important to remember the last level must always have DEEP_LEVEL=0 (to stop further backend propagation).
An example of such a deployment on a kubernetes environment is provided [here](https://github.com/eartvit/mlasp-etsad).
