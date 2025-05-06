package com.computer.demoComputer.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.computer.demoComputer.domain.Product;
import com.computer.demoComputer.service.ProductService;
import com.computer.demoComputer.service.UploadService;

import jakarta.validation.Valid;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService) {
        this.productService = productService;
        this.uploadService = uploadService;
    }

    @GetMapping("/admin/product")
    private String getProductPage(Model model,
                                  @RequestParam(value = "page") Optional<String> pageOptional) {

        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {

            }
        } catch (Exception e) {
            
        }

        Pageable pageable = PageRequest.of(page - 1, 2);
        Page<Product> pageHavProducts = this.productService.getAllProducts(pageable);
        List<Product> allProducts = pageHavProducts.getContent();

        model.addAttribute("products", allProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("allPages", pageHavProducts.getTotalPages());

        return "admin/product/tableProduct";
    }

    @GetMapping("/admin/product/create")
    private String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/createProduct";
    }

    @GetMapping("/admin/product/{id}")
    private String getProductDetailPage(Model model, @PathVariable long id) {
        Product showProductById = this.productService.getProductById(id).get();
        model.addAttribute("product", showProductById);
        model.addAttribute("id", id);
        return "admin/product/detailProduct";
    }

    @GetMapping("/admin/product/update/{id}")
    private String getUpdateProductPage(Model model, @PathVariable long id) {
        Product showProductById = this.productService.getProductById(id).get();
        model.addAttribute("updateProduct", showProductById);
        model.addAttribute("id", id);
        return "admin/product/updateProduct";
    }

    @GetMapping("/admin/product/delete/{id}")
    private String getDeleteProductPage(Model model, @PathVariable long id) {
        Optional<Product> showProductById = this.productService.getProductById(id);
        model.addAttribute("deleteProduct", showProductById.get());
        model.addAttribute("id", id);
        return "admin/product/deleteProduct";
    }

    @PostMapping("/admin/product/create")
    private String getProductPageAfterCreate(@ModelAttribute("newProduct") @Valid Product product,
            BindingResult newProductBindingResult,
            @RequestParam("myFile") MultipartFile multipartFile,
            Model model) {

        List<FieldError> errors = newProductBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + "-" + error.getDefaultMessage());
        }

        if (newProductBindingResult.hasErrors()) {
            model.addAttribute("newProduct", product);
            return "admin/product/createProduct";
        }

        String image = this.uploadService.handleSaveUploadImage(multipartFile);
        product.setImage(image);

        this.productService.handleSaveProduct(product);
        return "redirect:/admin/product";
    }

    @PostMapping("/admin/product/update")
    private String getProductPageAfterUpdate(@ModelAttribute("updateProduct") @Valid Product product,
                                             BindingResult updateProductBindingResult,
                                             @RequestParam("myFile") MultipartFile multipartFile,
                                             Model model) {

        String image = this.uploadService.handleSaveUploadImage(multipartFile);
        Product currentProduct = this.productService.getProductById(product.getId()).get();

        List<FieldError> errors = updateProductBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + "-" + error.getDefaultMessage());
        }

        if (updateProductBindingResult.hasErrors()) {
            product.setImage(currentProduct.getImage());
            model.addAttribute("updateProduct", product);
            return "admin/product/updateProduct";
        }

        if (currentProduct != null) {
            currentProduct.setName(product.getName());
            currentProduct.setPrice(product.getPrice());
            currentProduct.setFactory(product.getFactory());
            currentProduct.setTarget(product.getTarget());
            currentProduct.setDetailDesc(product.getDetailDesc());
            currentProduct.setShortDesc(product.getShortDesc());
            currentProduct.setQuantity(product.getQuantity());

            if (!multipartFile.isEmpty()) {
                currentProduct.setImage(image);
            }
            this.productService.handleSaveProduct(currentProduct);
        }

        return "redirect:/admin/product";
    }

    @PostMapping("/admin/product/delete")
    private String getProductPageAfterDelete(@ModelAttribute("deleteProduct") Product product) {

        Product currentProduct = this.productService.getProductById(product.getId()).get();
        if (currentProduct != null) {
            this.productService.deleteProductById(product.getId());
        }
        return "redirect:/admin/product";
    }
}
