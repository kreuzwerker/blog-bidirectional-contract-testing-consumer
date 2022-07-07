package de.kreuzwerker.blogs.bidirectionalprovider;

import de.kreuzwerker.blogs.bidirectionalprovider.exception.CustomException;
import de.kreuzwerker.blogs.bidirectionalprovider.objects.Employee;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DemoService {

    public static final String INVALIDID = "faaaf9e7-dfa7-4561-97a5-21756290383a";
    public static final String UNKNOWNID = "3535c952-1c46-4cc7-9908-c4a3a1e5c597";
    public static final String BADREQUESTID = "00d6e488-7552-48e6-a082-47d059c77ee2";

    public static final String KNOWNUSER = "user@known.com";
    public static final String BADRESPONSE = "user@dropped.com";

    public List<Employee> getEmployees(String departmentId) {
        switch(departmentId) {
            case INVALIDID -> {
                // create a response that will not match the schema defintion, because email is a required field
                Employee emp1 = Employee.builder().firstName("Jane").lastName("Doe").email("jane.dow@whoknows.com").employeeId(UUID.randomUUID()).build();
                Employee emp2 = Employee.builder().firstName("John").lastName("Doe").email("doe@whoknows.com").employeeId(UUID.randomUUID()).build();
                Employee emp3 = Employee.builder().firstName("Robin").lastName("Doe").employeeId(UUID.randomUUID()).build();
                return List.of(emp1, emp2, emp3);
            }
            case UNKNOWNID -> {
                // throw not found error
                throw new CustomException(404, "Department Id not found");
            }
            case BADREQUESTID -> {
                // throw error with response code not defined in schema
                throw new CustomException(400, "Never liked that department anyway");
            }
            default -> {
                // create a response that matches the schema and is harmless
                Employee emp1 = Employee.builder().firstName("Jane").lastName("Doe").email("jane.dow@whoknows.com").employeeId(UUID.randomUUID()).build();
                Employee emp2 = Employee.builder().firstName("John").lastName("Doe").email("doe@whoknows.com").employeeId(UUID.randomUUID()).build();
                Employee emp3 = Employee.builder().firstName("Robin").lastName("Doe").email("robin@doe.com").employeeId(UUID.randomUUID()).build();
                return List.of(emp1, emp2, emp3);
            }
        }
    }

    public Employee createEmployee(String departmentId, Employee employee) {
        if(UNKNOWNID.equals(departmentId)) {
            throw new CustomException(404, "Department Id not found");
        }
        switch(employee.getEmail()) {
            case KNOWNUSER -> {
                // let's pretend this email is already in use and reject the entry
                throw new CustomException(400, "Email address already in use");
            }
            case BADRESPONSE -> {
                // let's return an object that is invalid according to schema by dropping the email
                return Employee.builder()
                  .firstName(employee.getFirstName())
                  .lastName(employee.getLastName())
                  .employeeId(UUID.randomUUID())
                  .build();
            }
            default -> {
                return Employee.builder()
                  .employeeId(UUID.randomUUID())
                  .firstName(employee.getFirstName())
                  .lastName(employee.getLastName())
                  .email(employee.getEmail())
                  .build();
            }
        }
    }

}
