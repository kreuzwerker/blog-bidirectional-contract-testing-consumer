package de.kreuzwerker.blogs.bidirectionalconsumer;

import de.kreuzwerker.blogs.bidirectionalconsumer.objects.Employee;
import de.kreuzwerker.blogs.bidirectionalconsumer.objects.EmployeesListing;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Service
@Slf4j
public class DemoClient {
  public static final String BASE_URI = "/demo-service/v1/";

  private final WebClient webClient;
  private final Duration requestTimeout;
  private final int maxRequestRetryAttempts;

  public DemoClient(
      WebClient.Builder webClientBuilder,
      @Value("${service.url}") String serviceBaseUrl,
      @Value("${service.client.timeout:10000}") int requestRetryTimeoutMs,
      @Value("${service.client.retry.maxAttempts:5}") int maxRequestRetryAttempts) {

    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
            .responseTimeout(Duration.ofMillis(500))
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(500, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(500, TimeUnit.MILLISECONDS)));

    ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
    this.webClient =
        webClientBuilder
            .baseUrl(serviceBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(connector)
            .build();

    this.requestTimeout = Duration.ofMillis(requestRetryTimeoutMs);
    this.maxRequestRetryAttempts = maxRequestRetryAttempts;
  }

  public ResponseEntity<EmployeesListing> getEmployees(
      @PathVariable("organizationId") UUID departmentId) {
    return this.webClient
        .get()
        .uri(getEmployeePath(departmentId))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<EmployeesListing>() {})
        .retryWhen(getClientRetrySpec())
        .block();
  }

  public ResponseEntity<Employee> postEmployee(
      @PathVariable("departmentId") UUID departmentId, Employee employee) {

    MediaType mediaTypeWithCharset =
        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    return this.webClient
        .post()
        .uri(getEmployeePath(departmentId))
        .contentType(mediaTypeWithCharset)
        .bodyValue(employee)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<Employee>() {})
        .retryWhen(getClientRetrySpec())
        .block();
  }

  private boolean isHttpStatusBetween502And504(Throwable throwable) {
    if (throwable instanceof WebClientResponseException) {
      WebClientResponseException e = (WebClientResponseException) throwable;
      log.error(
          "response status: {}, response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
      int status = e.getStatusCode().value();
      return status >= 502 && status <= 504;
    }
    return false;
  }

  private RetryBackoffSpec getClientRetrySpec() {
    return Retry.fixedDelay(this.maxRequestRetryAttempts, Duration.ofMillis(100))
        .filter(this::isHttpStatusBetween502And504)
        .onRetryExhaustedThrow(
            ((retryBackoffSpec, retrySignal) -> {
              throw new RuntimeException();
            }));
  }

  public static String getBaseUrl(UUID departmentId) {
    return BASE_URI + "departments/" + departmentId;
  }

  public static String getEmployeePath(UUID departmentId) {
    String url = getBaseUrl(departmentId) + "/employees";
    log.info(url);
    return url;
  }
}
