package com.computer.demoComputer.controller.client;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.computer.demoComputer.domain.Product;
import com.computer.demoComputer.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {

    private final ProductService productService;
    
    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String getItemDetailPage(Model model, @PathVariable long id) {
        Optional<Product> showProductById = this.productService.getProductById(id);
        model.addAttribute("product", showProductById.get());
        model.addAttribute("id", id);
        return "client/product/item-detail";
    }

    @PostMapping("/add-product-from-home-to-cart/{id}")
    public String addProductFromHomeToCart(@PathVariable long id, HttpServletRequest request) {
        long productId = id;
        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, session, productId, 1);
        
        return "redirect:/";
    }

    @PostMapping("/add-product-from-products-to-cart/{id}")
    public String addProductFromProductsToCart(@PathVariable long id, 
                                               HttpServletRequest request, 
                                               @RequestParam("page") Optional<String> pageOptional) {

        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {

            }
        } catch (Exception e) {

        }

        long productId = id;
        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, session, productId, 1);
        
        return "redirect:/products?page=" + page;
    }

    @PostMapping("/add-product-detail-to-cart")
    public String addProductDetailToCart(HttpServletRequest request,
                                         @RequestParam("id") long id,
                                         @RequestParam("quantity") long quantity) {

        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, session, id, quantity);
        
        return "redirect:/product/" + id;
    }
}
