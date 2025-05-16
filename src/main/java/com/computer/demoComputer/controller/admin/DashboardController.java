package com.computer.demoComputer.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.computer.demoComputer.service.OrderService;
import com.computer.demoComputer.service.ProductService;
import com.computer.demoComputer.service.UserService;

@Controller
public class DashboardController {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public DashboardController(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/admin")
    public String getDashboardPage(Model model) {
        model.addAttribute("countUser", this.userService.countAllUsers());
        model.addAttribute("countProduct", this.productService.countAllProducts());
        model.addAttribute("countOrder", this.orderService.countAllOrders());
        return "admin/dashboard/showDashboard";
    }
}
