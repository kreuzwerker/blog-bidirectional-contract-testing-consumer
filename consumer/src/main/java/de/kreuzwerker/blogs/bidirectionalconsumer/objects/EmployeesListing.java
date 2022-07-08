package de.kreuzwerker.blogs.bidirectionalconsumer.objects;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeesListing {
  @NotNull List<Employee> employees;
}
