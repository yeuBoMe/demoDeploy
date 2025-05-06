package com.computer.demoComputer.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;    
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.computer.demoComputer.domain.Cart;
import com.computer.demoComputer.domain.CartDetail;
import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.OrderDetail;
import com.computer.demoComputer.domain.Product;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.domain.dto.ProductCriteriaDTO;
import com.computer.demoComputer.repository.CartDetailRepository;
import com.computer.demoComputer.repository.CartRepository;
import com.computer.demoComputer.repository.OrderDetailRepository;
import com.computer.demoComputer.repository.OrderRepository;
import com.computer.demoComputer.repository.ProductRepository;
import com.computer.demoComputer.service.specification.ProductSpecs;

import jakarta.servlet.http.HttpSession;

@Service
public class ProductService {
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public ProductService(
            CartRepository cartRepository,
            CartDetailRepository cartDetailRepository,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            ProductRepository productRepository,
            UserService userService) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Page<Product> getProductsWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
        if (productCriteriaDTO.getTargetOptional() == null 
         && productCriteriaDTO.getFactoryOptional() == null
         && productCriteriaDTO.getPriceOptional() == null
         && productCriteriaDTO.getSortOptional() == null) {
            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getFactoryOptional().isPresent()) {
            Specification<Product> currentSpec = ProductSpecs.listFactoryLike(productCriteriaDTO.getFactoryOptional().get());
            combinedSpec = combinedSpec.and(currentSpec);
        }

        if (productCriteriaDTO.getTargetOptional().isPresent()) {
            Specification<Product> currentSpec = ProductSpecs.listTargetLike(productCriteriaDTO.getTargetOptional().get());
            combinedSpec = combinedSpec.and(currentSpec);
        }

        if (productCriteriaDTO.getPriceOptional().isPresent()) {
            List<String> listPrice = productCriteriaDTO.getPriceOptional().get();
            Specification<Product> currentSpec = this.getProductsWithListPrice(listPrice);
            combinedSpec = combinedSpec.and(currentSpec);
        }

        return this.productRepository.findAll(combinedSpec, pageable);
    }

    public Page<Product> getProductsWithName(Pageable pageable, String name) {
        return this.productRepository.findAll(ProductSpecs.nameLike(name), pageable);
    }

    public Page<Product> getProductsWithFactory(Pageable pageable, String factory) {
        return this.productRepository.findAll(ProductSpecs.factoryLike(factory), pageable);
    }

    public Page<Product> getProductsWithMinPrice(Pageable pageable, double minPrice) {
        return this.productRepository.findAll(ProductSpecs.priceGreaterThanOrEqual(minPrice), pageable);
    }

    public Page<Product> getProductsWithMaxPrice(Pageable pageable, double maxPrice) {
        return this.productRepository.findAll(ProductSpecs.priceLessThanOrEqual(maxPrice), pageable);
    }

    public Page<Product> getProductsWithListFactory(Pageable pageable, List<String> listFactory) {
        return this.productRepository.findAll(ProductSpecs.listFactoryLike(listFactory), pageable);
    }

    public Page<Product> getProductsWithPriceRange(Pageable pageable, String price) {
        double minVal = 0;
        Double maxVal = null;

        if (price.equals("duoi-10-trieu")) {
            minVal = 0;
            maxVal = 10_000_000.0;
        } else if (price.equals("10-den-20-trieu")) {
            minVal = 10_000_000;
            maxVal = 20_000_000.0;
        } else if (price.equals("20-den-30-trieu")) {
            minVal = 20_000_000;
            maxVal = 30_000_000.0;
        } else if (price.equals("tren-30-trieu")) {
            minVal = 30_000_000;
        } else {
            return this.productRepository.findAll(pageable);
        }

        if (maxVal != null) {
            return this.productRepository.findAll(ProductSpecs.priceRange(minVal, maxVal), pageable);
        } else {
            return this.productRepository.findAll(ProductSpecs.priceGreaterThanOrEqual(minVal), pageable);
        }
    }

    public Specification<Product> getProductsWithListPrice(List<String> listPrice) {
        Specification<Product> combinedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();

        for (String p : listPrice) {
            double min = 0;
            Double max = null;

            switch (p) {
                case "duoi-10-trieu":
                    min = 0;
                    max = 10_000_000.0;
                    break;
                case "10-den-20-trieu":
                    min = 10_000_000;
                    max = 20_000_000.0;
                    break;
                case "20-den-30-trieu":
                    min = 20_000_000;
                    max = 30_000_000.0;
                    break;
                case "tren-30-trieu":
                    min = 30_000_000;
                    break;
            }

            if (max != null) {
                Specification<Product> rangeSpec = ProductSpecs.multiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
                
            } else {
                Specification<Product> minSpec = ProductSpecs.priceGreaterThanOrEqual(min);
                combinedSpec = combinedSpec.or(minSpec);
            }
        }

        return combinedSpec; 
    }

    public Optional<Product> getProductById(long id) {
        return this.productRepository.findById(id);
    }

    public void deleteProductById(long id) {
        this.productRepository.deleteById(id);
    }

    public long countAllProducts() {
        return this.productRepository.count();
    }

    public void handleAddProductToCart(String email, HttpSession session, long productId, long quantity) {

        User userGetByEmail = this.userService.getUserByEmail(email);
        if (userGetByEmail != null) {

            Cart showCartByUser = this.cartRepository.findByUser(userGetByEmail);
            if (showCartByUser == null) {
                Cart cart = new Cart();
                cart.setSum(0);
                cart.setUser(userGetByEmail);
                showCartByUser = this.cartRepository.save(cart);
            }

            Optional<Product> showProductById = this.productRepository.findById(productId);
            if (showProductById.isPresent()) {
                Product product = showProductById.get();

                CartDetail oldCartDetail = this.cartDetailRepository.findByCartAndProduct(showCartByUser, product);
                if (oldCartDetail == null) {
                    CartDetail cartDetail = new CartDetail();
                    cartDetail.setCart(showCartByUser);
                    cartDetail.setProduct(product);
                    cartDetail.setQuantity(quantity);
                    cartDetail.setPrice(product.getPrice());
                    this.cartDetailRepository.save(cartDetail);

                    int sum = showCartByUser.getSum() + 1;
                    showCartByUser.setSum(sum);
                    this.cartRepository.save(showCartByUser);
                    session.setAttribute("sum", sum);
                } else {
                    oldCartDetail.setQuantity(oldCartDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldCartDetail);
                }
            }
        }
    }

    public void handleRemoveCartDetail(long cartDetailId, HttpSession session) {
        Optional<CartDetail> showCartDetailById = this.cartDetailRepository.findById(cartDetailId);
        if (showCartDetailById.isPresent()) {
            CartDetail cartDetail = showCartDetailById.get();
            Cart currentCart = cartDetail.getCart();

            this.cartDetailRepository.deleteById(cartDetailId);
            if (currentCart.getSum() > 1) {
                int sum = currentCart.getSum() - 1;
                currentCart.setSum(sum);
                this.cartRepository.save(currentCart);
                session.setAttribute("sum", sum);
            } else {
                this.cartRepository.deleteById(currentCart.getId());
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cd : cartDetails) {
            Optional<CartDetail> showCartDetailById = this.cartDetailRepository.findById(cd.getId());
            if (showCartDetailById.isPresent()) {
                CartDetail cartDetail = showCartDetailById.get();
                cartDetail.setQuantity(cd.getQuantity());
                this.cartDetailRepository.save(cartDetail);
            }
        }
    }

    public void handlePlaceOrder(User user,
            HttpSession session,
            String receiverName, String receiverAddress, String receiverPhone) {

        // create orderdetail
        Cart showCartByUser = this.cartRepository.findByUser(user);
        if (showCartByUser != null) {
            List<CartDetail> cartDetails = showCartByUser.getCartDetails();
            if (cartDetails != null) {

                // step: create order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);

                double totalPrice = 0;
                for (CartDetail cd : cartDetails) {
                    totalPrice += (cd.getPrice() * cd.getQuantity());
                }
                order.setTotalPrice(totalPrice);
                order.setStatus("PENDING");
                order = this.orderRepository.save(order);

                for (CartDetail cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setPrice(cd.getPrice());
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setQuantity(cd.getQuantity());
                    this.orderDetailRepository.save(orderDetail);
                }

                for (CartDetail cd : cartDetails) {
                    this.cartDetailRepository.deleteById(cd.getId());
                }

                this.cartRepository.deleteById(showCartByUser.getId());
                session.setAttribute("sum", 0);
            }
        }
    }
}
