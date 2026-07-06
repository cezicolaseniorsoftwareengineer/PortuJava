package com.biotechpay.lab.application;

import com.biotechpay.lab.domain.User;
import com.biotechpay.lab.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Long requiredId = Objects.requireNonNull(id, "id");
        return userRepository.findById(requiredId)
                .orElseThrow(() -> new RuntimeException("User not found: " + requiredId));
    }

    public User getUserByUsername(String username) {
        String requiredUsername = Objects.requireNonNull(username, "username");
        return userRepository.findByUsername(requiredUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + requiredUsername));
    }

    @Transactional
    public User createUser(String username, String email, String password) {
        String requiredUsername = Objects.requireNonNull(username, "username");
        String requiredEmail = Objects.requireNonNull(email, "email");
        String requiredPassword = Objects.requireNonNull(password, "password");
        if (userRepository.existsByUsername(requiredUsername)) {
            throw new RuntimeException("Username already exists: " + requiredUsername);
        }
        if (userRepository.existsByEmail(requiredEmail)) {
            throw new RuntimeException("Email already exists: " + requiredEmail);
        }

        User user = new User(requiredUsername, requiredEmail, passwordEncoder.encode(requiredPassword));
        return userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(Objects.requireNonNull(username, "username"));
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(Objects.requireNonNull(email, "email"));
    }

    private static final String DEFAULT_USERNAME = "local-player";

    /**
     * This is a single-local-user practice tool (see SecurityConfig: no login flow exists), so every
     * request implicitly acts as this one seeded player rather than resolving an authenticated principal.
     */
    @Transactional
    public synchronized User getOrCreateDefaultUser() {
        return userRepository.findByUsername(DEFAULT_USERNAME)
                .orElseGet(() -> userRepository.save(new User(
                        DEFAULT_USERNAME,
                        "local-player@biotechpay.local",
                        passwordEncoder.encode("local-dev-only"))));
    }
}
