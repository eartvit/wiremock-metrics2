package org.concordia.eartvit.research;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.Gauge;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class WireMockMetrics {

    public static boolean TRACE = false;
    public static boolean DEEP_LEVEL = false;
    public static String DEEP_ENDPOINT = "";

    public static void main(String[] args) {

        String trace = System.getenv().getOrDefault("TRACEACTIVE", "False");

        DEEP_LEVEL = Boolean.parseBoolean(System.getenv().getOrDefault("DEEP_LEVEL", "false"));
        DEEP_ENDPOINT = System.getenv().getOrDefault("DEEP_ENDPOINT", "http://localhost:8081/mock");

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        if (trace.equalsIgnoreCase("True")) {
            WireMockMetrics.TRACE = true;
            System.out.println("Hello world, tracing is active!");
        }

        int lowerBound = Integer.valueOf(System.getenv().getOrDefault("DELAYLOWERBOUNDMS", "100"));
        int upperBound = Integer.valueOf(System.getenv().getOrDefault("DELAYUPPERBOUNDMS", "300"));

        // WireMock config options
        int port = Integer.valueOf(System.getenv().getOrDefault("WIREMOCKPORT", "8080"));
        int prometheusPort = Integer.valueOf(System.getenv().getOrDefault("PROMETHEUSPORT", "9090"));
        int containerThreads = Integer.valueOf(System.getenv().getOrDefault("CONTAINERTHREADS", "125"));
        boolean asyncRespEnabled = Boolean.valueOf(System.getenv().getOrDefault("ASYNCRESPENABLED", "true"));
        int asyncRespThreads = Integer.valueOf(System.getenv().getOrDefault("ASYNCRESPTHREADS", "25"));
        int jettyAcceptors = Integer.valueOf(System.getenv().getOrDefault("JETTYACCEPTORS", "110"));
        int jettyAcceptQSize = Integer.valueOf(System.getenv().getOrDefault("JETTYACCEPTORSQSIZE", "1000"));


        if (WireMockMetrics.TRACE) {
            System.out.println("Using the following configuration:");
            System.out.println("\tDELAYLOWERBOUNDMS:" + lowerBound);
            System.out.println("\tDELAYUPPERBOUNDMS:" + upperBound);
            System.out.println("\tWIREMOCKPORT:" + port);
            System.out.println("\tCONTAINERTHREADS:" + containerThreads);
            System.out.println("\tASYNCRESPENABLED:" + asyncRespEnabled);
            System.out.println("\tASYNCRESPTHREADS:" + asyncRespThreads);
            System.out.println("\tJETTYACCEPTORS:" + jettyAcceptors);
            System.out.println("\tJETTYACCEPTORSQSIZE:" + jettyAcceptQSize);
        }

        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(port)
                .asynchronousResponseEnabled(asyncRespEnabled)
                .asynchronousResponseThreads(asyncRespThreads)
                .containerThreads(containerThreads)
                .jettyAcceptors(jettyAcceptors)
                .jettyAcceptQueueSize(jettyAcceptQSize)
                .extensions(new RandomBodyStringResponseTransformer()));

        wireMockServer.stubFor(post(urlPathEqualTo("/mock"))
                        .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withUniformRandomDelay(lowerBound, upperBound)
                        ));

        wireMockServer.stubFor(post(urlPathEqualTo("/mock"))
                        .withQueryParam("id", matching("[0-9]+")) // Match the "id" parameter with a regex
                        .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withUniformRandomDelay(lowerBound, upperBound))
                        );

        DefaultExports.initialize();

        HTTPServer prometheusServer = null;

        Gauge systemLoadAverage = Gauge.build()
                .name("system_load_average")
                .help("System Load Average")
                .register();

        // keep the wiremock server running forever
        try {
            wireMockServer.start();
            prometheusServer = new HTTPServer(prometheusPort);

            if (WireMockMetrics.TRACE){
                System.out.println("Prometheus server started on port: " + prometheusPort + ". Metrics available at /metrics");
            }

            while (true) {
                if (!wireMockServer.isRunning()) {
                    wireMockServer.start();
                }

                //capture the system load average from the main thread
                double crtSystemLoadAverage = osBean.getSystemLoadAverage();
                systemLoadAverage.set(crtSystemLoadAverage);

                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            prometheusServer.close();
        }
    }
}
