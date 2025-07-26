package com.harsh.ecommerce.repository;

import com.harsh.ecommerce.entity.Role;
import com.harsh.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByFirstNameAndLastName(String firstName, String lastName);

    List<User> findByCreatedAtAfter(LocalDateTime date);

    List<User> findByEnabled(boolean enabled);

    List<User> findByEmailContainingIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt >= :date")
    List<User> findRecentUsersByRole(@Param("role") Role role, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query(value = "SELECT * FROM users WHERE email LIKE %:domain%", nativeQuery = true)
    List<User> findByEmailDomain(@Param("domain") String domain);

    void deleteByRole(Role role);
}
