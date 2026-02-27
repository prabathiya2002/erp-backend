package com.erp.finance.repository;

import com.erp.finance.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(User.UserRole role);
    List<User> findByStatus(User.UserStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
