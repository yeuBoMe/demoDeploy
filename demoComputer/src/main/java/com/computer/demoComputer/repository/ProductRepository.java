package com.computer.demoComputer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.computer.demoComputer.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    Product save(Product product);
    
    Optional<Product> findById(long id);
    
    void deleteById(long id);
    
    long count();

    Page<Product> findAll(Pageable pageable);

    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
} 
