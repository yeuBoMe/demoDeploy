package com.computer.demoComputer.service;

import com.computer.demoComputer.DemoComputerApplication;
import com.computer.demoComputer.domain.*;
import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.repository.*;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = DemoComputerApplication.class)
@ActiveProfiles("test")
@Transactional
@ExtendWith(UserServiceIntegrationTest.TestResultWatcher.class)
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

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
        cell.setCellValue("INTEGRATION TEST RESULTS - UserService");

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
        String filePath = "target/UserServiceIntegrationTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/UserServiceIntegrationTest.xlsx");
        if (oldFile.exists()) {
            oldFile.delete();
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password123");
        user.setAddress("123 Street");
        return user;
    }

    @Test
    void testGetUserByEmail_Found() {
        User user = createValidUser();
        userRepository.save(user);

        User result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        User result = userService.getUserByEmail("notfound@example.com");

        assertNull(result);
    }

    @Test
    void testCheckEmailExist_Exists() {
        User user = createValidUser();
        user.setEmail("abc@example.com");
        userRepository.save(user);

        boolean result = userService.checkEmailExist("abc@example.com");

        assertTrue(result);
    }

    @Test
    void testCheckEmailExist_NotExists() {
        boolean result = userService.checkEmailExist("notexist@example.com");

        assertFalse(result);
    }

    @Test
    void testGetAllUsers() {
        User user = createValidUser();
        user.setEmail("user@example.com");
        userRepository.save(user);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("user@example.com", result.getContent().get(0).getEmail());
    }

    @Test
    void testGetUserById_Found() {
        User user = createValidUser();
        user.setEmail("user@example.com");
        user = userRepository.save(user);

        Optional<User> result = userService.getUserById(user.getId());

        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        Optional<User> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCountAllUsers() {
        User user = createValidUser();
        user.setEmail("user@example.com");
        userRepository.save(user);

        long count = userService.countAllUsers();

        assertEquals(1L, count);
    }

    @Test
    void testHandleSaveUser() {
        User user = createValidUser();
        user.setEmail("save@example.com");

        User savedUser = userService.handleSaveUser(user);

        assertNotNull(savedUser);
        assertEquals("save@example.com", savedUser.getEmail());
    }

    @Test
    void testDeleteUserById_UserExists_NoOrder_NoCart() {
        User user = createValidUser();
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        HttpSession mockSession = mock(HttpSession.class);

        userService.deleteUserById(user.getId(), mockSession);

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testDeleteUserById_UserNotFound() {
        HttpSession mockSession = mock(HttpSession.class);

        userService.deleteUserById(999L, mockSession);
    }

    @Test
    void testDeleteUserById_UserHasOrder() {
        User user = createValidUser();
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(100.0);
        order.setReceiverName("Receiver");
        order.setReceiverAddress("456 Street");
        order.setReceiverPhone("1234567890");
        order.setStatus("PENDING");
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setPrice(50.0);
        orderDetail.setQuantity(1L);
        order.setOrderDetails(List.of(orderDetail));
        order = orderRepository.save(order);
        orderDetailRepository.save(orderDetail);

        HttpSession mockSession = mock(HttpSession.class);

        userService.deleteUserById(user.getId(), mockSession);

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
        assertEquals(0, orderRepository.count());
        assertEquals(0, orderDetailRepository.count());
    }

    @Test
    void testDeleteUserById_UserHasCartAndCartDetail() {
        User user = createValidUser();
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setSum(100);
        CartDetail cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setPrice(50.0);
        cartDetail.setQuantity(1L);
        cart.setCartDetails(List.of(cartDetail));
        cart = cartRepository.save(cart);
        cartDetailRepository.save(cartDetail);

        HttpSession mockSession = mock(HttpSession.class);

        userService.deleteUserById(user.getId(), mockSession);

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
        assertEquals(0, cartRepository.count());
        assertEquals(0, cartDetailRepository.count());
    }

    @Test
    void testDeleteUserById_UserHasOrderAndCart() {
        User user = createValidUser();
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(100.0);
        order.setReceiverName("Receiver");
        order.setReceiverAddress("456 Street");
        order.setReceiverPhone("1234567890");
        order.setStatus("PENDING");
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setPrice(50.0);
        orderDetail.setQuantity(1L);
        order.setOrderDetails(List.of(orderDetail));
        order = orderRepository.save(order);
        orderDetailRepository.save(orderDetail);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setSum(100);
        CartDetail cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setPrice(50.0);
        cartDetail.setQuantity(1L);
        cart.setCartDetails(List.of(cartDetail));
        cart = cartRepository.save(cart);
        cartDetailRepository.save(cartDetail);

        HttpSession mockSession = mock(HttpSession.class);

        userService.deleteUserById(user.getId(), mockSession);

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
        assertEquals(0, orderRepository.count());
        assertEquals(0, orderDetailRepository.count());
        assertEquals(0, cartRepository.count());
        assertEquals(0, cartDetailRepository.count());
    }

    @Test
    void testGetAllRoles() {
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        List<Role> result = userService.getAllRoles();

        assertEquals(1, result.size());
        assertEquals("ROLE_USER", result.get(0).getName());
    }

    @Test
    void testGetRoleById_Found() {
        Role role = new Role();
        role.setName("ROLE_USER");
        role = roleRepository.save(role);

        Role result = userService.getRoleById(role.getId());

        assertNotNull(result);
        assertEquals("ROLE_USER", result.getName());
    }

    @Test
    void testGetRoleById_NotFound() {
        Role result = userService.getRoleById(999L);

        assertNull(result);
    }
}