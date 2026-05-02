package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.api.dto.CreateUserRequest;
import com.onnick.reservationcamps.api.dto.IdResponse;
import com.onnick.reservationcamps.api.dto.LoginRequest;
import com.onnick.reservationcamps.api.dto.LoginResponse;
import com.onnick.reservationcamps.api.dto.UserResponse;
import com.onnick.reservationcamps.api.dto.UserSummaryResponse;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.NotFoundException;
import com.onnick.reservationcamps.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
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

    @GetMapping("/search")
    public List<UserSummaryResponse> search(@RequestParam("q") @NotBlank String q) {
        return userService.searchByEmail(q).stream().map(u -> new UserSummaryResponse(u.getId(), u.getEmail())).toList();
    }

    @GetMapping("/recent")
    public List<UserSummaryResponse> recent() {
        return userService.recentUsers().stream().map(u -> new UserSummaryResponse(u.getId(), u.getEmail())).toList();
    }

    @GetMapping
    public List<UserSummaryResponse> list() {
        // Demo endpoint: list a bounded number of users so the UI can offer a pick-list.
        return userService.listUsers(200).stream().map(u -> new UserSummaryResponse(u.getId(), u.getEmail())).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(@Valid @RequestBody CreateUserRequest request) {
        // Security note: end users cannot self-assign ADMIN via request body.
        var user = userService.createUser(request.email(), request.password(), UserRole.CUSTOMER);
        return new IdResponse(user.getId());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var user = userService.login(request.email(), request.password());
        return new LoginResponse(user.getId(), user.getRole());
    }
}
