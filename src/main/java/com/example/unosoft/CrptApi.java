package com.example.unosoft;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

public class CrptApi {

    private final Semaphore requestSemaphore;
    private final long intervalInMillis;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestSemaphore = new Semaphore(requestLimit, true);
        this.intervalInMillis = timeUnit.toMillis(1);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(intervalInMillis);
                    requestSemaphore.release(requestSemaphore.drainPermits());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void createDocument(String document, String signature) {
        try {
            requestSemaphore.acquire();

            performApiRequest(document, signature);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            requestSemaphore.release();
        }
    }

    private void performApiRequest(String document, String signature) {
        try {
            URL apiUrl = new URI("https://ismp.crpt.ru/api/v3/lk/documents/create").toURL();
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = "{\"description\":{\"participantInn\":\"string\"},\"doc_id\":\"string\",\"doc_status\"" +
                    ":\"string\",\"doc_type\":\"LP_INTRODUCE_GOODS\",\"importRequest\":true,\"owner_inn\":\"string\"," +
                    "\"participant_inn\":\"string\",\"producer_inn\":\"string\",\"production_date\":\"2020-01-23\",\"" +
                    "production_type\":\"string\",\"products\":[{\"certificate_document\":\"string\",\"" +
                    "certificate_document_date\":\"2020-01-23\",\"certificate_document_number\":\"string\",\"owner_inn\"" +
                    ":\"string\",\"producer_inn\":\"string\",\"production_date\":\"2020-01-23\",\"tnved_code\":\"string\"" +
                    ",\"uit_code\":\"string\",\"uitu_code\":\"string\"}],\"reg_date\":\"2020-01-23\",\"reg_number\":\"string\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (true) {
                    crptApi.createDocument("Sample document", "Signature");
                }
            }).start();
        }
    }
}
