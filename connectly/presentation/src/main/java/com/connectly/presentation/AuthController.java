package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    record RegisterRequest(
            @NotBlank @Size(max = 60) String firstName,
            @NotBlank @Size(max = 60) String lastName,
            @NotBlank @Size(min = 3, max = 40)
            @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "dozvoljena su samo slova, brojevi, '_' i '.'") String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password) {}

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    private final Register register;
    private final Login login;
    AuthController(Register register, Login login) { this.register = register; this.login = login; }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    AuthResult register(@Valid @RequestBody RegisterRequest r) {
        return register.register(new RegisterCommand(r.firstName(), r.lastName(), r.username(), r.email(), r.password()));
    }

    @PostMapping("/login")
    AuthResult login(@Valid @RequestBody LoginRequest r) {
        return login.login(new LoginCommand(r.email(), r.password()));
    }
}
