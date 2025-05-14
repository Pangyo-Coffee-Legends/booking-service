package com.nhnacademy.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ConfirmPasswordRequest {

    @NotBlank
    private String password;

}
