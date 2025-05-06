package com.computer.demoComputer.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.OrderDetail;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.repository.OrderDetailRepository;
import com.computer.demoComputer.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return this.orderRepository.findAll(pageable);
    }

    public Optional<Order> getOrderById(long id) {
        return this.orderRepository.findById(id);
    }

    public Order handleSaveOrder(Order order) {
        return this.orderRepository.save(order);
    }

    public long countAllOrders() {
        return this.orderRepository.count();
    }

    public List<Order> getListOrderByUser(User user) {
        return this.orderRepository.findByUser(user);
    }

    public void deleteOrderById(long id) {
        Optional<Order> showOrderById = this.orderRepository.findById(id);
        if (showOrderById.isPresent()) {
            Order order = showOrderById.get();

            List<OrderDetail> orderDetails = order.getOrderDetails();
            for (OrderDetail orderDetail : orderDetails) {
                this.orderDetailRepository.deleteById(orderDetail.getId());
            }
        }

        this.orderRepository.deleteById(id);
    }

}
