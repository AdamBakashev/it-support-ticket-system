package de.ticketsystem.service;

import de.ticketsystem.domain.User;
import de.ticketsystem.dto.CreateUserRequest;
import de.ticketsystem.dto.UserResponse;
import de.ticketsystem.exception.BusinessValidationException;
import de.ticketsystem.exception.ResourceNotFoundException;
import de.ticketsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessValidationException("Benutzername '" + request.username() + "' ist bereits vergeben.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessValidationException("E-Mail '" + request.email() + "' wird bereits verwendet.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(
                request.username(),
                request.email(),
                encodedPassword,
                request.role(),
                request.department()
        );

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer mit ID " + id + " wurde nicht gefunden."));
    }
}
