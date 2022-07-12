package de.kreuzwerker.blogs.bidirectionalconsumer;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kreuzwerker.blogs.bidirectionalconsumer.objects.Employee;
import de.kreuzwerker.blogs.bidirectionalconsumer.objects.EmployeesListing;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith({PactConsumerTestExt.class, SpringExtension.class})
@PactTestFor(providerName = "bidirectional-provider", port = "20999")
@ActiveProfiles("test")
@AutoConfigureWebClient
public class UpstreamPactTest {

  @Autowired private WebClient.Builder webClientBuilder;

  @Autowired
  ObjectMapper mapper = new ObjectMapper();

  private DemoClient demoClient;

  private final UUID departmentId = UUID.fromString("6a7e41b9-cacf-44f4-95b7-af1fdd60f3c8");
  private final UUID unknownDepartmentId = UUID.fromString("3535c952-1c46-4cc7-9908-c4a3a1e5c597");

  @Pact(consumer = "pact-consumer")
  public RequestResponsePact pactEmployeeListing(PactDslWithProvider builder) {

    DslPart responseBody =
        newJsonBody(
                (body) -> {
                  body.minArrayLike(
                      "employees",
                      1,
                      emp -> {
                        emp.stringType("firstName", "Ellen");
                        emp.stringType("lastName", "Ripley");
                        emp.stringType("email", "ripley@weyland-yutani.com");
                        emp.uuid("employeeId", UUID.randomUUID());
                      });
                })
            .build();

    return builder
        .given("employees exist for department")
        .uponReceiving("a request to list all employees for department")
        .path("/demo-service/v1/departments/" + departmentId + "/employees")
        .method("GET")
        .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
        .willRespondWith()
        .status(200)
        .body(responseBody)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "pactEmployeeListing", pactVersion = PactSpecVersion.V3)
  void shouldGetEmployees() {
    EmployeesListing resultListing = demoClient.getEmployees(departmentId).getBody();
    assertThat(resultListing).isNotNull();
    assertThat(resultListing.getEmployees().size()).isGreaterThan(0);
  }

  @Pact(consumer = "pact-consumer")
  public RequestResponsePact pactEmployeeListingDeptNotFound(PactDslWithProvider builder) {

    return builder
        .given("employees exist for department")
        .uponReceiving("a request to list all employees for an unknown department")
        .path("/demo-service/v1/departments/" + unknownDepartmentId + "/employees")
        .method("GET")
        .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
        .willRespondWith()
        .status(404)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "pactEmployeeListingDeptNotFound", pactVersion = PactSpecVersion.V3)
  void shouldReturnNotFoundForEmployees() {

    WebClientResponseException ex =
        assertThrows(
            WebClientResponseException.class, () -> demoClient.getEmployees(unknownDepartmentId));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Pact(consumer = "pact-consumer")
  public RequestResponsePact pactEmployeeListingBadRequest(PactDslWithProvider builder) {
    return builder
        .given("the user does something stupid")
        .uponReceiving("a nonsense request")
        .path("/demo-service/v1/departments/" + unknownDepartmentId + "/employees")
        .method("GET")
        .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
        .willRespondWith()
        .status(400)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "pactEmployeeListingBadRequest", pactVersion = PactSpecVersion.V3)
  void shouldReturnBadRequestForEmployees() {

    WebClientResponseException ex =
        assertThrows(
            WebClientResponseException.class, () -> demoClient.getEmployees(unknownDepartmentId));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Pact(consumer = "pact-consumer")
  public RequestResponsePact pactEmployeeCreateMinimalData(PactDslWithProvider builder)
      throws JsonProcessingException {
    DslPart responseBody =
        newJsonBody(
                (body) -> {
                  body.stringType("firstName", "Ellen");
                  body.stringType("email", "ripley@weyland-yutani.com");
                  body.uuid("employeeId", UUID.randomUUID());
                })
            .build();

    return builder
        .given("the department exists")
        .uponReceiving("a request to create a new employee with minimal required data")
        .path("/demo-service/v1/departments/" + departmentId + "/employees")
        .method("POST")
        .body(new JSONObject(mapper.writeValueAsString(createEmployeeMinData())))
        .willRespondWith()
        .body(responseBody)
        .status(201)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "pactEmployeeCreateMinimalData", pactVersion = PactSpecVersion.V3)
  void shouldCreateEmployeeMinimal() {
    Employee result = demoClient.postEmployee(departmentId, createEmployeeMinData()).getBody();
    assertThat(result).isNotNull();
  }

  @Pact(consumer = "pact-consumer")
  public RequestResponsePact pactEmployeeCreateFullData(PactDslWithProvider builder)
      throws JsonProcessingException {
    DslPart responseBody =
        newJsonBody(
                (body) -> {
                  body.stringType("firstName", "Ellen");
                  body.stringType("lastName", "Ripley");
                  body.stringType("email", "ripley@weyland-yutani.com");
                  body.uuid("employeeId", UUID.randomUUID());
                })
            .build();

    return builder
        .given("the department exists")
        .uponReceiving("a request to create a new employee with all data")
        .path("/demo-service/v1/departments/" + departmentId + "/employees")
        .method("POST")
        .body(new JSONObject(mapper.writeValueAsString(createEmployeeFullData())))
        .willRespondWith()
        .body(responseBody)
        .status(201)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "pactEmployeeCreateFullData", pactVersion = PactSpecVersion.V3)
  void shouldCreateEmployee() {
    Employee result = demoClient.postEmployee(departmentId, createEmployeeFullData()).getBody();
    assertThat(result).isNotNull();
  }

  private Employee createEmployeeFullData() {
    Employee emp = new Employee();
    emp.setFirstName("Simone");
    emp.setLastName("Giertz");
    emp.setEmail("simone@best-robots.com");
    return emp;
  }

  private Employee createEmployeeMinData() {
    Employee emp = new Employee();
    emp.setFirstName("Michelle");
    emp.setEmail("michelle.yeoh@goat.com");
    return emp;
  }

  @BeforeEach
  void setup(MockServer mockServer) {
    this.demoClient = new DemoClient(webClientBuilder, mockServer.getUrl(), 1000, 3000);
  }
}
