package com.computer.demoComputer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.computer.demoComputer.domain.Cart;
import com.computer.demoComputer.domain.User;

import jakarta.transaction.Transactional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart save(Cart cart);
    Cart findByUser(User user);

    @Modifying
    @Transactional
    @Query("Delete from Cart c where c.id = :id")
    void deleteById(long id);
}
