package by.bsuir.growpathserver.notification.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraineeUserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String patronymicName;
}
