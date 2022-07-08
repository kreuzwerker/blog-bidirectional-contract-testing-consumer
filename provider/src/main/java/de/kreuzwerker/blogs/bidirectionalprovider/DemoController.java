package de.kreuzwerker.blogs.bidirectionalprovider;

import de.kreuzwerker.blogs.bidirectionalprovider.exception.CustomException;
import de.kreuzwerker.blogs.bidirectionalprovider.objects.Employee;
import de.kreuzwerker.blogs.bidirectionalprovider.objects.EmployeesResult;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(
    path = {"/demo-service/v1/"},
    produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class DemoController {

  DemoService demoService;

  @GetMapping(path = "departments/{departmentId}/employees")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Department not found"),
    @ApiResponse(responseCode = "500", description = "my brain! it's broken!")
  })
  public ResponseEntity<EmployeesResult> getMethodDemo(
      @PathVariable("departmentId") UUID departmentId) {
    try {
      List<Employee> employees = demoService.getEmployees(departmentId.toString());
      return new ResponseEntity<>(
          EmployeesResult.builder().employees(employees).build(), HttpStatus.OK);
    } catch (CustomException ce) {
      throw new ResponseStatusException(HttpStatus.resolve(ce.getCode()), ce.getMessage());
    }
  }

  @PostMapping(
      path = "departments/{departmentId}/employees",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User created"),
    @ApiResponse(responseCode = "400", description = "Email address already in use"),
    @ApiResponse(responseCode = "500", description = "my brain! it's broken!")
  })
  public ResponseEntity<Employee> postMethodDemo(
      @RequestBody Employee createEmployee, @PathVariable("departmentId") UUID departmentId) {
    try {
      Employee createdEmployee =
          demoService.createEmployee(departmentId.toString(), createEmployee);
      return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    } catch (CustomException ce) {
      throw new ResponseStatusException(HttpStatus.resolve(ce.getCode()), ce.getMessage());
    }
  }
}
