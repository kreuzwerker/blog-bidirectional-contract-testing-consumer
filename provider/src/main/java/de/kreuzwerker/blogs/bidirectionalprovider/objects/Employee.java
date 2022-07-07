package de.kreuzwerker.blogs.bidirectionalprovider.objects;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Employee {

    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    private UUID employeeId;
}
