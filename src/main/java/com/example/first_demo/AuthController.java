package com.example.first_demo;
import com.example.first_demo.model.ApiResponse;
import com.example.first_demo.model.LoginRequest;
import com.example.first_demo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
            Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
        if (user.isEmpty()){

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found"));
        }
        User userData = user.get();
        if (!userData.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false,"Wrong password"));
        }
        return ResponseEntity.ok(new ApiResponse(true, "Login Successful"));
    }
}