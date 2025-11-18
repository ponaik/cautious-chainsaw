package com.intern.gateway.dto;

import java.time.LocalDate;

public record UserRegistrationRequest(
        String name,
        String surname,
        String email,
        String username,
        String password,

        LocalDate birthDate
){}
