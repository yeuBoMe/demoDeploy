package com.computer.demoComputer.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.computer.demoComputer.domain.Cart;
import com.computer.demoComputer.domain.CartDetail;
import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.Role;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.domain.dto.RegisterDTO;
import com.computer.demoComputer.repository.CartDetailRepository;
import com.computer.demoComputer.repository.CartRepository;
import com.computer.demoComputer.repository.OrderDetailRepository;
import com.computer.demoComputer.repository.OrderRepository;
import com.computer.demoComputer.repository.RoleRepository;
import com.computer.demoComputer.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(CartRepository cartRepository,
            CartDetailRepository cartDetailRepository,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(long id) {
        return this.userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    // public void deleteUserById(long id) {
    // this.userRepository.deleteById(id);
    // }

    // @Transactional
    // public void deleteUserById(long id, HttpSession session) {
    // Optional<User> userGetById = this.userRepository.findById(id);
    // if (userGetById.isPresent()) {
    // User user = userGetById.get();

    // List<Order> orders = this.orderRepository.findByUser(user);
    // if (orders != null) {
    // for (Order order : orders) {
    // List<OrderDetail> orderDetails = order.getOrderDetails();
    // for (OrderDetail od : orderDetails) {
    // this.orderDetailRepository.deleteById(od.getId());
    // }

    // this.orderRepository.deleteById(order.getId());
    // }
    // }

    // Cart cartGetByUser = this.cartRepository.findByUser(user);
    // if (cartGetByUser != null) {
    // List<CartDetail> cartDetails = cartGetByUser.getCartDetails();
    // for (CartDetail cd : cartDetails) {
    // this.cartDetailRepository.deleteById(cd.getId());
    // }

    // this.cartRepository.deleteById(cartGetByUser.getId());
    // session.setAttribute("sum", 0);
    // }
    // }

    // this.userRepository.deleteById(id);
    // }

    @Transactional
    public void deleteUserById(long id, HttpSession session) {
        Optional<User> userGetById = this.userRepository.findById(id);
        if (userGetById.isPresent()) {
            User user = userGetById.get();

            // Xóa tất cả đơn hàng và chi tiết đơn hàng trước
            List<Order> orders = this.orderRepository.findByUser(user);
            if (orders != null && !orders.isEmpty()) {
                for (Order order : orders) {
                    this.orderDetailRepository.deleteAll(order.getOrderDetails()); // Xóa OrderDetail trước
                }
                this.orderRepository.deleteAll(orders); // Xóa Order sau
            }

            // Xóa giỏ hàng và chi tiết giỏ hàng
            Cart cartGetByUser = this.cartRepository.findByUser(user);
            if (cartGetByUser != null) {
                List<CartDetail> cartDetails = cartGetByUser.getCartDetails();
                if (cartDetails != null && !cartDetails.isEmpty()) {
                    this.cartDetailRepository.deleteAll(cartDetails); // Xóa CartDetail trước
                }
                this.cartRepository.delete(cartGetByUser); // Xóa Cart sau
            }

            // Cuối cùng, xóa user
            this.userRepository.delete(user);
            session.setAttribute("sum", 0); // Reset giỏ hàng về 0
        }
    }

    public List<Role> getAllRoles() {
        return this.roleRepository.findAll();
    }

    public Role getRoleById(long id) {
        return this.roleRepository.findById(id);
    }

    public User registerDTOtoUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setFullName(registerDTO.getFirstName() + " " + registerDTO.getLastName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());
        return user;
    }

    public boolean checkEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public long countAllUsers() {
        return this.userRepository.count();
    }
}
