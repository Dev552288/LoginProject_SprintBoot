package com.example.first_demo.api;
import com.example.first_demo.model.*;
import com.example.first_demo.repo.UserRepository;
import com.example.first_demo.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
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
            Optional<User> user
                    = userRepository.findByUsername(loginRequest.getUsername());
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
        // Generate Access Token
        String accessToken   = JwtUtil.generateAccessToken(userData.getUsername());

        // Generate Refresh Token
        String refreshToken =
                JwtUtil.generateRefreshToken(userData.getUsername());

        // Prepare response
        Map<String, Object> response = new HashMap<>();

        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 900);

        return ResponseEntity.ok(new ApiResponse(true, "Login Successful", response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUp (@RequestBody SignUpRequest signUpRequest){
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,"Username already exists"));
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false,"Email already exists"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(signUpRequest.getPassword());
        user.setEmail(signUpRequest.getEmail());
        user.setMobileNumber(signUpRequest.getMobileNumber());
        user.setDob(signUpRequest.getDob());
        user.setName(signUpRequest.getName());

        userRepository.save(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User Created"));
    }


    @PostMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(
            @RequestHeader("Authorization") String authHeader
    ){
        try {
            String token = authHeader.replace("Bearer","");
            String userName = JwtUtil.validateToken(token);
            return ResponseEntity.ok(new ApiResponse(true, "Welcome", userName));
        }catch (Exception e){
            return ResponseEntity.ok(new ApiResponse(false, "Invalid Token"));
        }
    }


    @PostMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // 1. Cleanly extract token whether "Bearer " is present or not
            String token = authHeader;
            if (token != null && token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7);
            }
            token = token.trim(); // Removes leading/trailing spaces

            // 2. Validate Token
            String userName = JwtUtil.validateToken(token);

            // 3. Find User
            Optional<User> user = userRepository.findByUsername(userName);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }

            User userData = user.get();
            Map<String, Object> map = new HashMap<>();

            // 4. Safely populate user info (Map.of throws NullPointerException if any value is null!)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", userData.getName() != null ? userData.getName() : "");
            userInfo.put("email", userData.getEmail() != null ? userData.getEmail() : "");
            userInfo.put("username", userData.getUsername());

            map.put("userInfo", userInfo);
            map.put("status", Map.of(
                    "totalTasks", 12,
                    "pendingActions", 3,
                    "notificationsCount", 5
            ));

            return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", map));
        } catch (Exception e) {
            // Print exception in backend console to see the exact root cause
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid Token: " + e.getMessage()));
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        try {

            String refreshToken = request.getRefreshToken();

            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false,
                                "Refresh token is required"));
            }

            // Validate refresh token
            String username =
                    JwtUtil.validateRefreshToken(refreshToken);

            // Check user
            Optional<User> user =
                    userRepository.findByUsername(username);

            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "User not found"));
            }

            // Generate new access token
            String newAccessToken =
                    JwtUtil.generateAccessToken(username);

            Map<String, Object> response = new HashMap<>();

            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 900);

            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "Token refreshed successfully",
                            response
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(
                            false,
                            "Invalid or expired refresh token"
                    ));
        }
    }

}