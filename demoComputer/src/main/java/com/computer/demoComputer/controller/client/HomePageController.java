package com.computer.demoComputer.controller.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.Product;
import com.computer.demoComputer.domain.Product_;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.domain.dto.ProductCriteriaDTO;
import com.computer.demoComputer.service.OrderService;
import com.computer.demoComputer.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomePageController {

    private final ProductService productService;
    private final OrderService orderService;

    public HomePageController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String getHomePage(Model model) {

        Pageable pageable = PageRequest.of(0, 4);
        Page<Product> pageHavProducts = this.productService.getAllProducts(pageable);
        List<Product> allProducts = pageHavProducts.getContent();
        model.addAttribute("products", allProducts);
        return "client/homepage/home";
    }

    @GetMapping("/order-history")
    public String getOrderHistoryPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");

        User user = new User();
        user.setId(id);

        List<Order> orders = this.orderService.getListOrderByUser(user);
        model.addAttribute("orders", orders);

        return "client/cart/order-history";
    }

    @GetMapping("/products")
    public String getProductsPage(Model model,
                                  HttpServletRequest request,
                                  @RequestParam(value = "page", required = false) Optional<String> pageOptional,
                                  @RequestParam(value = "name", required = false) Optional<String> nameOptional,
                                  @RequestParam(value = "factory", required = false) Optional<String> factoryOptional,
                                  @RequestParam(value = "price", required = false) Optional<String> priceOptional,
                                  @RequestParam(value = "target", required = false) Optional<String> targetOptional,
                                  @RequestParam(value = "sort", required = false) Optional<String> sortOptional) {

        ProductCriteriaDTO productCriteriaDTO = new ProductCriteriaDTO();
        productCriteriaDTO.setPageOptional(pageOptional);
        productCriteriaDTO.setNameOptional(nameOptional);
        productCriteriaDTO.setSortOptional(sortOptional);

        if (factoryOptional.isPresent()) {
            List<String> listFactory = Arrays.asList(factoryOptional.get().split(","));
            productCriteriaDTO.setFactoryOptional(Optional.of(listFactory));
        } else {
            productCriteriaDTO.setFactoryOptional(Optional.empty());
        }

        if (targetOptional.isPresent()) {
            List<String> listTarget = Arrays.asList(targetOptional.get().split(","));
            productCriteriaDTO.setTargetOptional(Optional.of(listTarget));
        } else {
            productCriteriaDTO.setTargetOptional(Optional.empty());
        }
        
        if (priceOptional.isPresent()) {
            List<String> listPrice = Arrays.asList(priceOptional.get().split(","));
            productCriteriaDTO.setPriceOptional(Optional.of(listPrice));
        } else {
            productCriteriaDTO.setPriceOptional(Optional.empty());
        }

        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {

            }
        } catch (Exception e) {

        }

        Pageable pageable;
        if (sortOptional.isPresent()) {
            String sort = sortOptional.get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 5, Sort.by(Product_.PRICE).ascending());
                productCriteriaDTO.setSortOptional(Optional.of(sort));
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 5, Sort.by(Product_.PRICE).descending());
                productCriteriaDTO.setSortOptional(Optional.of(sort));
            } else {
                pageable = PageRequest.of(page - 1, 10);
                productCriteriaDTO.setSortOptional(Optional.empty());
            }
        } else {
            pageable = PageRequest.of(page - 1, 10);
            productCriteriaDTO.setSortOptional(Optional.empty());
        }

        Page<Product> pageHavProducts = this.productService.getProductsWithSpec(pageable, productCriteriaDTO);
        List<Product> listProduct = pageHavProducts.getContent().size() > 0 ? pageHavProducts.getContent()
                                                                            : new ArrayList<Product>();
        
        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            qs = qs.replace("page=" + page, "");
        }
    
        model.addAttribute("products", listProduct);
        model.addAttribute("currentPage", page);
        model.addAttribute("allPages", pageHavProducts.getTotalPages());
        model.addAttribute("queryString", qs);

        return "client/product/all-products";
    }
}
