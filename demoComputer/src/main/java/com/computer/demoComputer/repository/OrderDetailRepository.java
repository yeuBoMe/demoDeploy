package com.computer.demoComputer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.computer.demoComputer.domain.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    OrderDetail save(OrderDetail orderDetail);
    void deleteById(long id);
}
