package com.computer.demoComputer.service;

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
import com.computer.demoComputer.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(ProductServiceIntegrationTest.TestResultWatcher.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductServiceIntegrationTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    private ProductService productService;

    private static Workbook workbook;
    private static Sheet sheet;
    private static List<TestResult> testResults = new ArrayList<>();
    private static int passCount = 0;
    private static int failCount = 0;
    private static int skippedCount = 0;

    private static class TestResult {
        String testName;
        String status;

        TestResult(String testName, String status) {
            this.testName = testName;
            this.status = status;
        }
    }

    static class TestResultWatcher implements TestWatcher {
        @Override
        public void testSuccessful(ExtensionContext context) {
            testResults.add(new TestResult(context.getDisplayName(), "PASSED"));
            passCount++;
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            testResults.add(new TestResult(context.getDisplayName(), "FAILED"));
            failCount++;
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            testResults.add(new TestResult(context.getDisplayName(), "SKIPPED"));
            skippedCount++;
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            testResults.add(new TestResult(context.getDisplayName(), "ABORTED"));
            failCount++;
        }
    }

    @BeforeAll
    static void setupExcel() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Integration Test Results");

        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("INTEGRATION TEST RESULTS - ProductService");

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    @AfterAll
    static void writeExcel() throws IOException {
        int total = passCount + failCount + skippedCount;

        Row passRow = sheet.createRow(1);
        passRow.createCell(0).setCellValue("Passed: " + passCount + " test cases");

        Row failRow = sheet.createRow(2);
        failRow.createCell(0).setCellValue("Failed: " + failCount + " test cases");

        Row skippedRow = sheet.createRow(3);
        skippedRow.createCell(0).setCellValue("Skipped: " + skippedCount + " test cases");

        Row totalRow = sheet.createRow(4);
        totalRow.createCell(0).setCellValue("Total: " + total + " test cases");

        Row detailHeader = sheet.createRow(6);
        CellStyle headerStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        headerStyle.setFont(boldFont);

        Cell testNameCell = detailHeader.createCell(0);
        testNameCell.setCellValue("Test Case Name");
        testNameCell.setCellStyle(headerStyle);

        Cell statusCell = detailHeader.createCell(1);
        statusCell.setCellValue("Status");
        statusCell.setCellStyle(headerStyle);

        for (int i = 0; i < testResults.size(); i++) {
            Row row = sheet.createRow(7 + i);
            row.createCell(0).setCellValue(testResults.get(i).testName);
            row.createCell(1).setCellValue(testResults.get(i).status);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = "target/ProductServiceIntegrationTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/ProductServiceIntegrationTest.xlsx");
        if (oldFile.exists()) {
            oldFile.delete();
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    @BeforeEach
    void setUp() {
        productService = new ProductService(cartRepository, cartDetailRepository, orderRepository,
                orderDetailRepository, productRepository, userService);
        productRepository.deleteAll();
        cartDetailRepository.deleteAll();
        cartRepository.deleteAll();
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("Save product persists and returns product")
    void handleSaveProduct_SavesAndReturnsProduct() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");

        Product result = productService.handleSaveProduct(product);

        assertNotNull(result);
        assertNotNull(result.getId());
        Optional<Product> savedProduct = productRepository.findById(result.getId());
        assertTrue(savedProduct.isPresent());
        assertEquals("Laptop", savedProduct.get().getName());
    }

    @Test
    @Transactional
    @DisplayName("Get all products returns paged results")
    void getAllProducts_ReturnsPagedProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getAllProducts(PageRequest.of(0, 2));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with spec when no criteria returns all products")
    void getProductsWithSpec_WhenNoCriteria_ReturnsAllProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        ProductCriteriaDTO criteria = new ProductCriteriaDTO();
        criteria.setFactoryOptional(Optional.empty());
        criteria.setTargetOptional(Optional.empty());
        criteria.setPriceOptional(Optional.empty());
        criteria.setSortOptional(Optional.empty());
        Page<Product> result = productService.getProductsWithSpec(PageRequest.of(0, 2), criteria);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with spec when factory criteria returns filtered products")
    void getProductsWithSpec_WhenFactoryCriteria_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        ProductCriteriaDTO criteria = new ProductCriteriaDTO();
        criteria.setFactoryOptional(Optional.of(Arrays.asList("Dell")));
        criteria.setTargetOptional(Optional.empty());
        criteria.setPriceOptional(Optional.empty());
        criteria.setSortOptional(Optional.empty());
        Page<Product> result = productService.getProductsWithSpec(PageRequest.of(0, 2), criteria);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with spec when price criteria returns filtered products")
    void getProductsWithSpec_WhenPriceCriteria_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        ProductCriteriaDTO criteria = new ProductCriteriaDTO();
        criteria.setPriceOptional(Optional.of(Arrays.asList("10-den-20-trieu")));
        criteria.setFactoryOptional(Optional.empty());
        criteria.setTargetOptional(Optional.empty());
        criteria.setSortOptional(Optional.empty());
        Page<Product> result = productService.getProductsWithSpec(PageRequest.of(0, 2), criteria);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with name returns filtered products")
    void getProductsWithName_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getProductsWithName(PageRequest.of(0, 2), "Laptop");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with factory returns filtered products")
    void getProductsWithFactory_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getProductsWithFactory(PageRequest.of(0, 2), "Dell");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with min price returns filtered products")
    void getProductsWithMinPrice_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getProductsWithMinPrice(PageRequest.of(0, 2), 10_000_000.0);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with max price returns filtered products")
    void getProductsWithMaxPrice_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getProductsWithMaxPrice(PageRequest.of(0, 2), 20_000_000.0);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with list factory returns filtered products")
    void getProductsWithListFactory_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getProductsWithListFactory(PageRequest.of(0, 2), Arrays.asList("Dell"));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get products with price range 10-20 million returns filtered products")
    void getProductsWithPriceRange_10Den20Trieu_ReturnsFilteredProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        Page<Product> result = productService.getProductsWithPriceRange(PageRequest.of(0, 2), "10-den-20-trieu");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    @Transactional
    @DisplayName("Get product by ID returns product when exists")
    void getProductById_WhenProductExists_ReturnsProduct() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        product = productRepository.save(product);

        Optional<Product> result = productService.getProductById(product.getId());

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
    }

    @Test
    @Transactional
    @DisplayName("Get product by ID returns empty when not exists")
    void getProductById_WhenProductDoesNotExist_ReturnsEmpty() {
        Optional<Product> result = productService.getProductById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @DisplayName("Delete product by ID deletes product")
    void deleteProductById_DeletesProduct() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        product = productRepository.save(product);

        productService.deleteProductById(product.getId());

        assertFalse(productRepository.findById(product.getId()).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("Count all products returns correct count")
    void countAllProducts_ReturnsCount() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        productRepository.save(product);

        long result = productService.countAllProducts();

        assertEquals(1L, result);
    }

    @Test
    @Transactional
    @DisplayName("Add product to cart adds new cart detail")
    void handleAddProductToCart_AddsNewCartDetail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        user = userRepository.save(user);

        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        product = productRepository.save(product);

        HttpSession session = mock(HttpSession.class);
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        productService.handleAddProductToCart("test@example.com", session, product.getId(), 2L);

        Cart cart = cartRepository.findByUser(user);
        assertNotNull(cart);
        assertEquals(1, cart.getSum());
        List<CartDetail> cartDetails = cartDetailRepository.findAll();
        assertEquals(1, cartDetails.size());
        assertEquals(2L, cartDetails.get(0).getQuantity());
        verify(session).setAttribute("sum", 1);
    }

    @Test
    @Transactional
    @DisplayName("Remove cart detail removes detail and cart if last item")
    void handleRemoveCartDetail_RemovesCartDetail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        user = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setSum(1);
        cart = cartRepository.save(cart);

        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        product = productRepository.save(product);

        CartDetail cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setProduct(product);
        cartDetail.setQuantity(2L);
        cartDetail.setPrice(product.getPrice());
        cartDetail = cartDetailRepository.save(cartDetail);

        cart.setCartDetails(new ArrayList<>(Arrays.asList(cartDetail)));
        cart = cartRepository.save(cart);

        HttpSession session = mock(HttpSession.class);
        productService.handleRemoveCartDetail(cartDetail.getId(), session);

        assertFalse(cartDetailRepository.findById(cartDetail.getId()).isPresent());
        assertFalse(cartRepository.findById(cart.getId()).isPresent());
        verify(session).setAttribute("sum", 0);
    }

    @Test
    @Transactional
    @DisplayName("Update cart before checkout updates cart details")
    void handleUpdateCartBeforeCheckout_UpdatesCartDetails() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        user = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setSum(1);
        cart = cartRepository.save(cart);

        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        product = productRepository.save(product);

        CartDetail cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setProduct(product);
        cartDetail.setQuantity(2L);
        cartDetail.setPrice(product.getPrice());
        cartDetail = cartDetailRepository.save(cartDetail);

        CartDetail updatedCartDetail = new CartDetail();
        updatedCartDetail.setId(cartDetail.getId());
        updatedCartDetail.setQuantity(5L);

        productService.handleUpdateCartBeforeCheckout(Arrays.asList(updatedCartDetail));

        Optional<CartDetail> result = cartDetailRepository.findById(cartDetail.getId());
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getQuantity());
    }

    @Test
    @Transactional
    @DisplayName("Place order creates order and clears cart")
    void handlePlaceOrder_CreatesOrderAndClearsCart() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        user = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setSum(1);
        cart = cartRepository.save(cart);

        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(15_000_000.0);
        product.setFactory("Dell");
        product.setTarget("Gaming");
        product.setQuantity(10L);
        product.setSold(5L);
        product.setDetailDesc("High performance laptop");
        product.setShortDesc("Gaming laptop");
        product.setImage("laptop.jpg");
        product = productRepository.save(product);

        CartDetail cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setProduct(product);
        cartDetail.setQuantity(2L);
        cartDetail.setPrice(product.getPrice());
        cartDetail = cartDetailRepository.save(cartDetail);

        cart.setCartDetails(new ArrayList<>(Arrays.asList(cartDetail)));
        cart = cartRepository.save(cart);

        HttpSession session = mock(HttpSession.class);
        productService.handlePlaceOrder(user, session, "Receiver", "Address", "1234567890");

        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(user.getId(), order.getUser().getId());
        assertEquals("Receiver", order.getReceiverName());
        assertEquals("Address", order.getReceiverAddress());
        assertEquals("1234567890", order.getReceiverPhone());
        assertEquals(30_000_000.0, order.getTotalPrice());
        assertEquals("PENDING", order.getStatus());

        List<OrderDetail> orderDetails = orderDetailRepository.findAll();
        assertEquals(1, orderDetails.size());
        assertEquals(cartDetail.getQuantity(), orderDetails.get(0).getQuantity());
        assertEquals(cartDetail.getPrice(), orderDetails.get(0).getPrice());
        assertEquals(product.getId(), orderDetails.get(0).getProduct().getId());

        assertFalse(cartRepository.findById(cart.getId()).isPresent());
        assertFalse(cartDetailRepository.findById(cartDetail.getId()).isPresent());
        verify(session).setAttribute("sum", 0);
    }
}