package com.intern.gateway.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record UserResponse(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email
) implements Serializable {
}
