package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.api.dto.CreateUserRequest;
import com.onnick.reservationcamps.api.dto.IdResponse;
import com.onnick.reservationcamps.api.dto.UserResponse;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.NotFoundException;
import com.onnick.reservationcamps.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/by-email")
    public UserResponse byEmail(@RequestParam("email") @NotBlank @Email String email) {
        return userService
                .findByEmail(email)
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole()))
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(@Valid @RequestBody CreateUserRequest request) {
        var role = request.role() == null ? UserRole.CUSTOMER : request.role();
        var user = userService.createUser(request.email(), role);
        return new IdResponse(user.getId());
    }
}
