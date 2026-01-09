package com.example.eventmateai.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventmateai.model.User;
import com.example.eventmateai.repository.UserRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    // strong password: at least 8 chars, 1 lower, 1 upper, 1 digit, 1 symbol
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // simple in-memory OTP store: key = email, value = otp
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private final JavaMailSender mailSender;

    public AuthController(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    // ========== REGISTER ==========

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {

        // Check if email ALREADY exists for this specific role
        Optional<User> existing = userRepository.findByEmailAndRole(request.getEmail(), request.getRole());
        if (existing.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email already registered for this role"));
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            "Weak password. Use at least 8 characters with upper, lower, number and symbol."));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // TODO: hash later
        user.setRole(request.getRole());
        user.setFullName(request.getFullName());
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Registered successfully"));
    }

    // ========== LOGIN ==========

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Now we need role in the request to find the unique user account
        if (request.getRole() == null || request.getRole().isBlank()) {
            return ResponseEntity.badRequest().body(new LoginResponse("Role is required", null));
        }

        Optional<User> optionalUser = userRepository.findByEmailAndRole(request.getEmail(), request.getRole());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse("User not found for this role", null));
        }

        User user = optionalUser.get();

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse("Invalid password", null));
        }

        // Only send message + role, no credentials
        // Also send userId if needed by frontend
        LoginResponse resp = new LoginResponse("Login successful", user.getRole());
        resp.setUserId(user.getId());
        return ResponseEntity.ok(resp);
    }

    // ========== FORGOT PASSWORD (EMAIL ONLY, SEND OTP) ==========

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Email is required"));
        }

        // Check if ANY user exists with this email
        // We don't necessarily need the role here if we just want to verify identity
        // via email
        java.util.List<User> users = userRepository.findAllByEmail(email);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("No user found with this email"));
        }

        // generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(email, otp);

        // send email only
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your EventMate AI OTP");
            message.setText("Your OTP for password reset is: " + otp
                    + "\nIt is valid for this reset attempt.");
            mailSender.send(message);
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to send OTP email"));
        }

        return ResponseEntity.ok(new MessageResponse("OTP sent to email."));
    }

    // ========== RESET PASSWORD (VERIFY OTP) ==========

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        String email = request.getEmail();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();

        if (email == null || otp == null || newPassword == null ||
                email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("All fields are required"));
        }

        String storedOtp = otpStore.get(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired OTP"));
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(newPassword).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            "Weak password. Use at least 8 characters with upper, lower, number and symbol."));
        }

        // Update ALL accounts for this email (simplest approach for dual roles)
        java.util.List<User> users = userRepository.findAllByEmail(email);
        if (users.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("User not found"));
        }

        for (User u : users) {
            u.setPassword(newPassword);
            userRepository.save(u);
        }

        otpStore.remove(email);

        return ResponseEntity.ok(new MessageResponse("Password reset successful for all accounts with this email"));
    }

    // ========== DTO CLASSES ==========

    static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    static class LoginRequest {
        private String email;
        private String password;
        private String role; // NEW

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    static class LoginResponse {
        private String message;
        private String role;
        private Long userId; // NEW field added to response

        public LoginResponse(String message, String role) {
            this.message = message;
            this.role = role;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    static class ForgotPasswordRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    static class ResetPasswordRequest {
        private String email;
        private String otp;
        private String newPassword;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
