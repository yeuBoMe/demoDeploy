package com.computer.demoComputer.service;

import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.OrderDetail;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.repository.OrderDetailRepository;
import com.computer.demoComputer.repository.OrderRepository;
import com.computer.demoComputer.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(OrderServiceIntegrationTest.TestResultWatcher.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderServiceIntegrationTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private UserRepository userRepository;

    private OrderService orderService;

    // Excel report fields
    private static Workbook workbook;
    private static Sheet sheet;
    private static final List<TestResult> testResults = new ArrayList<>();
    private static int passCount = 0, failCount = 0, skippedCount = 0;

    // Struct to store result
    private static class TestResult {
        String testName;
        String status;
        TestResult(String testName, String status) {
            this.testName = testName;
            this.status = status;
        }
    }

    // Watch test results
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

    // Setup Excel summary
    @BeforeAll
    static void setupExcel() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Integration Test Results");

        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("INTEGRATION TEST RESULTS - OrderService");

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    // Write Excel to file
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
        String filePath = "target/OrderServiceIntegrationTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/OrderServiceIntegrationTest.xlsx");
        if (oldFile.exists()) oldFile.delete();

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
        workbook.close();
    }

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderDetailRepository);
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- TEST CASES ---------- //

    @Test
    @Transactional
    @DisplayName("Get all orders returns paged results")
    void getAllOrders_ReturnsPagedOrders() {
        User user = userRepository.save(createTestUser());
        orderRepository.saveAll(Arrays.asList(createTestOrder(user), createTestOrder(user)));

        Page<Order> result = orderService.getAllOrders(PageRequest.of(0, 2));

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(user.getId(), result.getContent().get(0).getUser().getId());
    }

    @Test
    @Transactional
    @DisplayName("Get order by ID returns order when exists")
    void getOrderById_WhenOrderExists_ReturnsOrder() {
        User user = userRepository.save(createTestUser());
        Order order = orderRepository.save(createTestOrder(user));

        Optional<Order> result = orderService.getOrderById(order.getId());

        assertTrue(result.isPresent());
        assertEquals(order.getId(), result.get().getId());
    }

    @Test
    @Transactional
    @DisplayName("Get order by ID returns empty when not exists")
    void getOrderById_WhenOrderDoesNotExist_ReturnsEmpty() {
        Optional<Order> result = orderService.getOrderById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @DisplayName("Save order persists and returns order")
    void handleSaveOrder_SavesAndReturnsOrder() {
        User user = userRepository.save(createTestUser());
        Order order = createTestOrder(user);

        Order result = orderService.handleSaveOrder(order);

        assertNotNull(result.getId());
        Optional<Order> saved = orderRepository.findById(result.getId());
        assertTrue(saved.isPresent());
        assertEquals(order.getTotalPrice(), saved.get().getTotalPrice());
    }

    @Test
    @Transactional
    @DisplayName("Count all orders returns correct count")
    void countAllOrders_ReturnsCount() {
        User user = userRepository.save(createTestUser());
        orderRepository.save(createTestOrder(user));
        orderRepository.save(createTestOrder(user));

        assertEquals(2, orderService.countAllOrders());
    }

    @Test
    @Transactional
    @DisplayName("Get orders by user returns correct orders")
    void getListOrderByUser_ReturnsOrders() {
        User user = userRepository.save(createTestUser());
        Order order = orderRepository.save(createTestOrder(user));

        List<Order> result = orderService.getListOrderByUser(user);

        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getId());
    }

    @Test
    @Transactional
    @DisplayName("Delete order by ID deletes order and details")
    void deleteOrderById_WhenOrderExists_DeletesOrderAndDetails() {
        User user = userRepository.save(createTestUser());
        OrderDetail detail = orderDetailRepository.save(createTestOrderDetail());

        Order order = createTestOrder(user);
        order.setOrderDetails(List.of(detail));
        order = orderRepository.save(order);

        detail.setOrder(order);
        orderDetailRepository.save(detail);

        orderService.deleteOrderById(order.getId());

        assertFalse(orderRepository.findById(order.getId()).isPresent());
        assertFalse(orderDetailRepository.findById(detail.getId()).isPresent());
    }

    @Test
    @Transactional
    @DisplayName("Delete non-existent order does nothing")
    void deleteOrderById_WhenOrderDoesNotExist_DoesNothing() {
        orderService.deleteOrderById(999L);
        assertEquals(0, orderRepository.count());
        assertEquals(0, orderDetailRepository.count());
    }

    // ---------- HELPER METHODS ---------- //

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password");
        user.setPhoneNumber("1234567890");
        return user;
    }

    private Order createTestOrder(User user) {
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(100.0);
        order.setReceiverName("Receiver");
        order.setReceiverAddress("Address");
        order.setReceiverPhone("1234567890");
        order.setStatus("PENDING");
        return order;
    }

    private OrderDetail createTestOrderDetail() {
        OrderDetail detail = new OrderDetail();
        detail.setQuantity(2);
        detail.setPrice(50.0);
        return detail;
    }
}