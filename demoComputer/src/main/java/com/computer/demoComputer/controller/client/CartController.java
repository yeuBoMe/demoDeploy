package com.computer.demoComputer.controller.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.computer.demoComputer.domain.Cart;
import com.computer.demoComputer.domain.CartDetail;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.service.CartService;
import com.computer.demoComputer.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");

        User user = new User();
        user.setId(id);

        Cart showCartByUser = this.cartService.getCartByUser(user);
        List<CartDetail> cartDetails = (showCartByUser != null) ? showCartByUser.getCartDetails()
                                                                : new ArrayList<CartDetail>();
        double totalPrice = 0;                                                        
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cart", showCartByUser);
        return "client/cart/cartDetail";
    }

    @GetMapping("/checkout")
    public String getCheckoutPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");

        User user = new User();
        user.setId(id);

        Cart showCartByUser = this.cartService.getCartByUser(user);
        List<CartDetail> cartDetails = (showCartByUser != null) ? showCartByUser.getCartDetails() 
                                                                : new ArrayList<CartDetail>();

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        return "client/cart/checkout";
    }

    @GetMapping("/thank-you")
    public String getThanksPage() {
        return "client/cart/thanks";
    }

    @PostMapping("/delete-cart/{id}")
    public String getCartPageAfterDelete(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailId = id;
        this.productService.handleRemoveCartDetail(cartDetailId, session);
        return "redirect:/cart";
    }

    @PostMapping("/confirm-checkout")
    public String confirmCheckout(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = (cart != null) ? cart.getCartDetails() : new ArrayList<CartDetail>();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(HttpServletRequest request,
                                   @RequestParam("receiverName") String receiverName, 
                                   @RequestParam("receiverAddress") String receiverAddress, 
                                   @RequestParam("receiverPhone") String receiverPhone) {

        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");

        User user = new User();
        user.setId(id);

        this.productService.handlePlaceOrder(user, session, receiverName, receiverAddress, receiverPhone);
        return "redirect:/thank-you";
    }
}
