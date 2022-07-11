package de.kreuzwerker.blogs.bidirectionalconsumer;

import de.kreuzwerker.blogs.bidirectionalconsumer.objects.Employee;
import de.kreuzwerker.blogs.bidirectionalconsumer.objects.EmployeesListing;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DemoService {

  private DemoClient client;

  public List<Employee> getEmployees(UUID departmentId) {
    ResponseEntity<EmployeesListing> response = client.getEmployees(departmentId);
    return response.getBody().getEmployees();
  }

  public Employee createEmployee(UUID departmentId, Employee employee) {
    ResponseEntity<Employee> response = client.postEmployee(departmentId, employee);
    return response.getBody();
  }
}
