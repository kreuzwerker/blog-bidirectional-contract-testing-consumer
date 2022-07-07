package de.kreuzwerker.blogs.bidirectionalprovider;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport.Level;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import de.kreuzwerker.blogs.bidirectionalprovider.objects.Employee;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith({SpringExtension.class})
class DemoControllerTest {

  @LocalServerPort private int randomServerPort;

  private final String specPath = "src/main/resources/openApi/openapi.json";

  private final OpenApiValidationFilter validationFilter = new OpenApiValidationFilter(specPath);

  @Test
  void shouldReturnList() {

    given()
        .port(randomServerPort)
        .filter(validationFilter)
        .when()
        .get("/demo-service/v1/departments/{departmentId}/employees", UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(200);
  }

  // expected to fail because response does not match schema
  @Test
  void shouldFailDueToResponse() {

    given()
        .port(randomServerPort)
        .filter(validationFilter)
        .when()
        .get("/demo-service/v1/departments/{departmentId}/employees", DemoService.INVALIDID)
        .then()
        .assertThat()
        .statusCode(200);
  }

  // expected to fail because path not correct
  @Test
  void shouldFailDueToPath() {

    given()
        .port(randomServerPort)
        .filter(validationFilter)
        .when()
        .get("/demo-service/v1/department/{departmentId}/employees", UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(200);
  }

  @Test
  void shouldCreateEmployee() throws JSONException {

    JSONObject requestObject = new JSONObject();
    requestObject.put("email", "email@address.com");
    requestObject.put("firstName", "Jane");
    requestObject.put("lastName", "Doe");

    Employee result =
        given()
            .port(randomServerPort)
            .filter(validationFilter)
            .when()
            .request()
            .contentType(ContentType.JSON)
            .body(requestObject.toString())
            .post("/demo-service/v1/departments/{departmentId}/employees", UUID.randomUUID())
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .body()
            .as(Employee.class);

    assertThat(result.getEmail()).isEqualTo("email@address.com");
  }

  @Test
  void shouldFailWithBadRequest() throws JSONException {

    /*
    validation changed to ignore response body because expected response matches error message and not defined body
    with validation.response on level ERROR, at least the response code is still verified against oas spec
     */
    OpenApiInteractionValidator validator =
        OpenApiInteractionValidator.createFor(specPath)
            .withLevelResolver(
                LevelResolver.create()
                    .withLevel("validation.request", Level.ERROR)
                    .withLevel("validation.schema.required", Level.INFO)
                    .withLevel("validation.response.body.missing", Level.INFO)
                    .withLevel("validation.response.body.schema.additionalProperties", Level.INFO)
                    .withLevel("validation.response.body.schema.required", Level.INFO)
                    .withLevel("validation.response", Level.ERROR)
                    .build())
            .build();

    OpenApiValidationFilter filter = new OpenApiValidationFilter(validator);

    JSONObject requestObject = new JSONObject();
    requestObject.put("email", DemoService.KNOWNUSER);
    requestObject.put("firstName", "Jane");
    requestObject.put("lastName", "Doe");

    given()
        .port(randomServerPort)
        .filter(filter)
        .when()
        .request()
        .contentType(ContentType.JSON)
        .body(requestObject.toString())
        .post("/demo-service/v1/departments/{departmentId}/employees", UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(400);
  }

  @Test
  void shouldFailDueInvalidResponse() throws JSONException {

    JSONObject requestObject = new JSONObject();
    requestObject.put("email", DemoService.BADRESPONSE);
    requestObject.put("firstName", "Jane");
    requestObject.put("lastName", "Doe");

    given()
        .port(randomServerPort)
        .filter(validationFilter)
        .when()
        .request()
        .contentType(ContentType.JSON)
        .body(requestObject.toString())
        .post("/demo-service/v1/departments/{departmentId}/employees", UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(201);
  }

  @Test
  void shouldFailDueInvalidRequest() throws JSONException {

    JSONObject requestObject = new JSONObject();
    requestObject.put("email", "someone@mail.com");
    requestObject.put("firstName", "Jane");

    given()
        .port(randomServerPort)
        .filter(validationFilter)
        .when()
        .request()
        .contentType(ContentType.JSON)
        .body(requestObject.toString())
        .post("/demo-service/v1/departments/{departmentId}/employees", UUID.randomUUID())
        .then()
        .assertThat()
        .statusCode(201);
  }
}
