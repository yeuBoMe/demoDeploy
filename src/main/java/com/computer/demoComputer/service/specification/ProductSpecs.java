package com.computer.demoComputer.service.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.computer.demoComputer.domain.Product;
import com.computer.demoComputer.domain.Product_;

import jakarta.persistence.criteria.Expression;

@Service
public class ProductSpecs {
    public static Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
            .like(criteriaBuilder.lower(
                root.get(Product_.NAME)), 
                "%" + name.toLowerCase() + "%"
            );
    }

    public static Specification<Product> factoryLike(String factory) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
            .like(criteriaBuilder.lower(
                root.get(Product_.FACTORY)), 
                "%" + factory.toLowerCase() + "%"
            );
    }

    public static Specification<Product> priceGreaterThanOrEqual(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
            .ge(root.get(Product_.PRICE), price);
    }

    public static Specification<Product> priceLessThanOrEqual(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
            .le(root.get(Product_.PRICE), price);
    }

    // public static Specification<Product> listFactoryLike(List<String> listFactory) {
    //     return (root, query, criteriaBuilder) -> {
    //         Expression<String> expressionFactory = criteriaBuilder.lower(root.get(Product_.FACTORY));

    //         List<String> listFactoryToLower = listFactory.stream()
    //                 .map(String::toLowerCase)
    //                 .toList();
        
    //         return expressionFactory.in(listFactoryToLower);
    //     };
    // }

    public static Specification<Product> listFactoryLike(List<String> listFactory) {
        return (root, query, criteriaBuilder) 
            -> criteriaBuilder.in(root.get(Product_.FACTORY)).value(listFactory); 
    }

    public static Specification<Product> listTargetLike(List<String> listTarget) {
        return (root, query, criteriaBuilder) 
            -> criteriaBuilder.in(root.get(Product_.TARGET)).value(listTarget); 
    }

    public static Specification<Product> priceRange(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
            .and(criteriaBuilder.ge(root.get(Product_.PRICE), min),
                 criteriaBuilder.le(root.get(Product_.PRICE), max)    
            );
    }

    public static Specification<Product> multiplePrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
            .between(root.get(Product_.PRICE), min, max);    
    }

}
