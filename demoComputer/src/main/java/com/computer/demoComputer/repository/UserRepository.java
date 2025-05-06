package com.computer.demoComputer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.computer.demoComputer.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(long id);

    User findByEmail(String email);

    void deleteById(long id);

    boolean existsByEmail(String email);

    long count();
}
