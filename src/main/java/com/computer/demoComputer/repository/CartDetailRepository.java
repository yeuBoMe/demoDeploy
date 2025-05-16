package com.computer.demoComputer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.computer.demoComputer.domain.Cart;
import com.computer.demoComputer.domain.CartDetail;
import com.computer.demoComputer.domain.Product;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    CartDetail save(CartDetail cartDetail);
    CartDetail existsByCartAndProduct(Cart cart, Product product);
    CartDetail findByCartAndProduct(Cart cart, Product product);
    Optional<CartDetail> findById(long id);
    void deleteById(long id);
}
