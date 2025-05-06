package com.computer.demoComputer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order save(Order order);

    Optional<Order> findById(long id);

    void deleteById(long id);

    long count();

    List<Order> findByUser(User user);

    Page<Order> findAll(Pageable pageable);
}
