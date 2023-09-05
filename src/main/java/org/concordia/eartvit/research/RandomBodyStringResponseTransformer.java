package org.concordia.eartvit.research;

import org.json.JSONObject;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import io.prometheus.client.Counter;

import java.net.URI;
//import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;


public class RandomBodyStringResponseTransformer extends ResponseTransformer {

    // Define custom metrics (e.g., request count)
    static final Counter requestCounter = Counter.build()
            .name("wiremock_requests_total")
            .help("Total number of requests handled by WireMock")
            .register();

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {

        int length = 10; // default sequence length
        requestCounter.inc();

        try {
            String id = request.queryParameter("id").firstValue();
            length = Integer.parseInt(id);
            if (WireMockMetrics.TRACE){
                System.out.println("Got id:" + length);
            }
        } catch (Exception e) {
            if (WireMockMetrics.TRACE) {
                System.out.println("Missing or malformed integer for id value, default 10 shall be used!");
            }
        }

        // Generate random content based on "id" length
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char randomChar = (char) (Math.random() * 26 + 'a');
            content.append(randomChar);
        }

        // Create a JSON object with the generated content
        JSONObject responseBody = new JSONObject();
        responseBody.put("content", content.toString());

        if (WireMockMetrics.TRACE) {
            System.out.println("Generating response payload size " + length + " characters as 'content': '" + content.toString() + "'");
        }

        if (WireMockMetrics.DEEP_LEVEL > 0) {
            // Call the other endpoint requesting the same payload as the one we must return
            // The payload we send uses a factor we must account for here

            String payloadString = "";
            if (WireMockMetrics.REQ_SIZE_FACTOR > 1) {
                int factor = length/WireMockMetrics.REQ_SIZE_FACTOR;
                if (factor == 0 )
                    factor = length;

                StringBuilder payload = new StringBuilder();
                for (int i = 0; i < factor; i++) {
                    char randomChar = (char) (Math.random() * 26 + 'a');
                    payload.append(randomChar);
                }
                payloadString = payload.toString();
            } else {
                payloadString = content.toString();
            }
            JSONObject requestBody = new JSONObject();
            requestBody.put("content", payloadString);
            try {
                String uri = WireMockMetrics.DEEP_ENDPOINT + "?id=" + length;
                if (WireMockMetrics.TRACE){
                    System.out.println("Generating deep request payload size " + payloadString.length() + " characters as 'content': '" + payloadString
                    + "' towards endpoint: " + uri);
                }
                HttpRequest deepRequest = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
                
                HttpClient deepClient = HttpClient.newHttpClient();
                HttpResponse<String> deepClientResponse = deepClient.send(deepRequest, BodyHandlers.ofString());

                if (deepClientResponse.statusCode() != 200){
                    if (WireMockMetrics.TRACE)
                        System.out.println("Request to " + uri + " failed with code: " + deepClientResponse.statusCode());
                }
                else {
                    if (WireMockMetrics.TRACE){
                        System.out.println("Got deep level response back: " + deepClientResponse.body().toString());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }            
        }

        return Response.Builder.like(response)
                .but()
                .body(responseBody.toString())
                .build();
    }

    @Override
    public String getName() {
        return "random-body-string-transformer";
    }

}
