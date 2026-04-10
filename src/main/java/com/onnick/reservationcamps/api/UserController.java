package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.api.dto.CreateUserRequest;
import com.onnick.reservationcamps.api.dto.IdResponse;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(@Valid @RequestBody CreateUserRequest request) {
        var role = request.role() == null ? UserRole.CUSTOMER : request.role();
        var user = userService.createUser(request.email(), role);
        return new IdResponse(user.getId());
    }
}

