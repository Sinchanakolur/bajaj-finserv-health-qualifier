package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private final WebClient webClient = WebClient.create();

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) {
		String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		String requestBody = "{\"name\": \"John Doe\", \"regNo\": \"REG12347\", \"email\": \"john@example.com\"}";

		ApiResponse response = webClient.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(ApiResponse.class)
				.block();

		if (response != null) {
			System.out.println("Webhook: " + response.getWebhook());
			System.out.println("AccessToken: " + response.getAccessToken());

			String finalQuery = """
              SELECT
                  e.EMP_ID,
                  e.FIRST_NAME,
                  e.LAST_NAME,
                  d.DEPARTMENT_NAME,
                  (SELECT COUNT(*)
                   FROM EMPLOYEE AS younger_e
                   WHERE younger_e.DEPARTMENT = e.DEPARTMENT AND younger_e.DOB > e.DOB
                  ) AS YOUNGER_EMPLOYEES_COUNT
              FROM EMPLOYEE AS e
              JOIN DEPARTMENT AS d ON e.DEPARTMENT = d.DEPARTMENT_ID
              ORDER BY e.EMP_ID DESC;
              """;

			String result = webClient.post()
					.uri(response.getWebhook())
					.header("Authorization", response.getAccessToken())
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue("{\"finalQuery\":\"" + finalQuery.replace("\n", " ").replace("\"", "\\\"") + "\"}")
					.retrieve()
					.bodyToMono(String.class)
					.block();

			System.out.println("Submission response: " + result);

		} else {
			System.err.println("Failed to get a valid response from the API.");
		}
	}
}