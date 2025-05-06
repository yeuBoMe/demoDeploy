package com.computer.demoComputer.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.service.OrderService;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/order")
    public String getTableOrderPage(Model model, 
                                    @RequestParam("page") Optional<String> pageOptional) {
        
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {

            }
        } catch (Exception e) {
            
        }

        Pageable pageable = PageRequest.of(page - 1, 2);
        Page<Order> pageHavOrders = this.orderService.getAllOrders(pageable);
        List<Order> allOrders = pageHavOrders.getContent();     

        model.addAttribute("orders", allOrders);
        model.addAttribute("currentPage", page);
        model.addAttribute("allPages", pageHavOrders.getTotalPages());
        return "admin/order/tableOrder";
    }

    @GetMapping("/admin/order/{id}")
    public String getDetailOrderPage(@PathVariable long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("order", this.orderService.getOrderById(id).get());
        model.addAttribute("orderDetails", this.orderService.getOrderById(id).get().getOrderDetails());
        return "admin/order/detailOrder";
    }

    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(@PathVariable long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("updateOrder", this.orderService.getOrderById(id).get());
        return "admin/order/updateOrder";
    }

    @GetMapping("/admin/order/delete/{id}")
    public String getDeleteOrderPage(@PathVariable long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("deleteOrder", this.orderService.getOrderById(id).get());
        return "admin/order/deleteOrder";
    }

    @PostMapping("/admin/order/update")
    public String getOrderPageAfterUpdate(@ModelAttribute("updateOrder") Order order) {
        Optional<Order> showOrderById = this.orderService.getOrderById(order.getId());
        if (showOrderById.isPresent()) {
            Order currentOrder = showOrderById.get();
            currentOrder.setStatus(order.getStatus());
            this.orderService.handleSaveOrder(currentOrder);
        }
        return "redirect:/admin/order";
    }

    @PostMapping("/admin/order/delete")
    public String getOrderPageAfterDelete(@ModelAttribute("deleteOrder") Order order) {
        this.orderService.deleteOrderById(order.getId());
        return "redirect:/admin/order";
    }

}
