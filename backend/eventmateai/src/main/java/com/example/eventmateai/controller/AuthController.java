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
    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

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

        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email already registered"));
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            "Weak password. Use at least 8 characters with upper, lower, number and symbol."
                    ));
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

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse("User not found", null));
        }

        User user = optionalUser.get();

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse("Invalid password", null));
        }

        // Only send message + role, no credentials
        return ResponseEntity.ok(
                new LoginResponse("Login successful", user.getRole())
        );
    }

    // ========== FORGOT PASSWORD (EMAIL ONLY, SEND OTP) ==========

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Email is required"));
        }

        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
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
                            "Weak password. Use at least 8 characters with upper, lower, number and symbol."
                    ));
        }

        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("User not found"));
        }

        User user = optUser.get();
        user.setPassword(newPassword); // TODO: hash later
        userRepository.save(user);

        otpStore.remove(email);

        return ResponseEntity.ok(new MessageResponse("Password reset successful"));
    }

    // ========== DTO CLASSES ==========

    static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    static class LoginRequest {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class LoginResponse {
        private String message;
        private String role;
        public LoginResponse(String message, String role) {
            this.message = message;
            this.role = role;
        }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    static class ResetPasswordRequest {
        private String email;
        private String otp;
        private String newPassword;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
