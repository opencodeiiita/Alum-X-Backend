package com.opencode.alumxbackend.users.repository;

import com.opencode.alumxbackend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;



///  every reposityr is tied a entity

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long userId);
    
    boolean existsById(Long userId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    long countByIdIn(Set<Long> ids);

}
