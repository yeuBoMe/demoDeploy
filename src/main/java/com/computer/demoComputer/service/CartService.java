package com.computer.demoComputer.service;

import org.springframework.stereotype.Service;

import com.computer.demoComputer.domain.Cart;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.repository.CartRepository;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCartByUser(User user) {
        return this.cartRepository.findByUser(user);
    }
}
