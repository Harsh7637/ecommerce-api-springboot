package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.PasswordChangeDto;
import com.harsh.ecommerce.dto.UserProfileUpdateDto;
import com.harsh.ecommerce.dto.UserRegistrationDto;
import com.harsh.ecommerce.dto.UserResponseDto;
import com.harsh.ecommerce.entity.Role;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.exception.EmailAlreadyExistsException;
import com.harsh.ecommerce.exception.InvalidPasswordException;
import com.harsh.ecommerce.exception.UserNotFoundException;
import com.harsh.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDto createUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + registrationDto.getEmail());
        }

        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setPhone(registrationDto.getPhone());
        user.setRole(registrationDto.getRole());

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return new UserResponseDto(user);
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return new UserResponseDto(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    public UserResponseDto updateUserProfile(Long id, UserProfileUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setPhone(updateDto.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponseDto::new);
    }

    public Page<UserResponseDto> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(UserResponseDto::new);
    }

    public Page<UserResponseDto> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable)
                .map(UserResponseDto::new);
    }

    public UserResponseDto updateUser(Long id, UserRegistrationDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(updateDto.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new EmailAlreadyExistsException("Email already exists: " + updateDto.getEmail());
            }
        }

        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setEmail(updateDto.getEmail());
        user.setPhone(updateDto.getPhone());

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    public void changePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        if (!passwordChangeDto.isNewPasswordConfirmed()) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setIsActive(true);
        userRepository.save(user);
    }

    public UserResponseDto updateUserRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        user.updateLastLogin();
        userRepository.save(user);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getUserCountByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public long getNewUsersCount(LocalDateTime since) {
        return userRepository.countNewUsersAfter(since);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserResponseDto> getInactiveUsers(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        return userRepository.findInactiveUsers(cutoffDate)
                .stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }
}