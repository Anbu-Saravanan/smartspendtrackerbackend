package com.SmartSpendExpense.controller;

import com.SmartSpendExpense.dto.request.*;
import com.SmartSpendExpense.dto.response.AuthResponse;
import com.SmartSpendExpense.model.RefreshToken;
import com.SmartSpendExpense.model.User;
import com.SmartSpendExpense.repository.UserRepository;
import com.SmartSpendExpense.security.JwtUtil;
import com.SmartSpendExpense.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // Helper: Generate random 6-digit OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100_000 + random.nextInt(900_000);
        return String.valueOf(otp);
    }


    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getId(), user.getEmail(),user.getRole());
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
                AuthResponse authResponse = new AuthResponse(token, user.getId(),
                        user.getEmail(),refreshToken.getToken(),user.getRole());
                return ResponseEntity.ok(authResponse);
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }


    // Registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEmailVerified(false);

        // Generate OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(new Date(System.currentTimeMillis() + 10 * 60 * 1000)); // 10 min expiry

        userRepository.save(user);

        // Send OTP Email (production ready)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your SmartSpend OTP Verification Code");
        message.setText("Your OTP code is: " + otp + "\nThis code expires in 10 minutes.");
        mailSender.send(message);

        // -- In production: send email using JavaMailSender --
        // For testing, print OTP in console
        System.out.println("OTP for " + user.getEmail() + " is " + otp);

        return ResponseEntity.ok("User Registered successfully. Check your email for OTP.");
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("No such user.");

        User user = userOpt.get();
        if (user.getEmailVerified()) return ResponseEntity.badRequest().body("Already verified!");

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            return ResponseEntity.badRequest().body("No OTP generated. Register again.");
        }
        if (user.getOtpExpiry().before(new Date())) {
            return ResponseEntity.badRequest().body("OTP expired. Register again.");
        }
        if (!user.getOtp().equals(req.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }

        // Verified!
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Email verified! You can now log in.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) return ResponseEntity.ok("If this email is registered, check your inbox.");
        User user = userOpt.get();

        // Generate token and expiry (1 hour)
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        userRepository.save(user);

        // Send email with link
        String resetLink = "/reset-password?token=" + token;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("SmartSpend Password Reset");
        msg.setText("Reset your password: " + resetLink + "\nThis link expires in 1 hour.");
        mailSender.send(msg);

        return ResponseEntity.ok("If this email is registered, check your inbox.");
    }



    //reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(req.getToken());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid or expired reset token.");

        User user = userOpt.get();
        if (user.getResetPasswordTokenExpiry() == null || user.getResetPasswordTokenExpiry().before(new Date())) {
            return ResponseEntity.badRequest().body("Reset token expired. Request again.");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successful! You can now log in.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> req) {
        String requestRefreshToken = req.get("refreshToken");

        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByToken(requestRefreshToken);
        if (refreshTokenOpt.isPresent() && refreshTokenOpt.get().getExpiryDate().isAfter(Instant.now())) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            User user = userRepository.findById(refreshToken.getUserId()).orElseThrow();
            String jwt = jwtUtil.generateToken( user.getId(),user.getEmail(),user.getRole());
            return ResponseEntity.ok(Map.of("accessToken", jwt, "refreshToken", requestRefreshToken));
        } else {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
    }
}
