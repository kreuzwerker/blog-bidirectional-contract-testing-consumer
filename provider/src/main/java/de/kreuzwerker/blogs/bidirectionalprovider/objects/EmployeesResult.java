package de.kreuzwerker.blogs.bidirectionalprovider.objects;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmployeesResult {

    @NotNull
    List<Employee> employees;

}
