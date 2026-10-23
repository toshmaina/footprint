package ke.co.skyworld.internship.domain.beans.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ke.co.skyworld.internship.util.formatting.Converter;

public record RegisterRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must be at least 8 characters")
        String password,

        @NotBlank(message = "full name is required")
        String fullName,

        @Positive(message = "warehouseId must be a positive number")
        Long warehouseId
) {
    // Normalize the email once, at construction, so validation runs
    // against the same canonical form the repository will store
    public RegisterRequest {
        email = Converter.normalizeEmail(email);
    }
}