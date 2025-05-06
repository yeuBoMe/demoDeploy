package com.computer.demoComputer.service;

import com.computer.demoComputer.domain.Order;
import com.computer.demoComputer.domain.OrderDetail;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.repository.OrderDetailRepository;
import com.computer.demoComputer.repository.OrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OrderServiceUnitTest.TestResultWatcher.class)
public class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private User user;
    private OrderDetail orderDetail;

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
        sheet = workbook.createSheet("Unit Test Results");

        Row headerRow = sheet.createRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("UNIT TEST RESULTS - OrderService");

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
        String filePath = "target/OrderServiceUnitTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/OrderServiceUnitTest.xlsx");
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
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        orderDetail = new OrderDetail();
        orderDetail.setId(1L);
        orderDetail.setQuantity(2);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalPrice(100.0);
        order.setOrderDetails(Arrays.asList(orderDetail));
    }

    @Test
    void getAllOrders_ReturnsPagedOrders() {
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(order));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(orderPage);

        Page<Order> result = orderService.getAllOrders(PageRequest.of(0, 2));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(order.getId(), result.getContent().get(0).getId());
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void getOrderById_WhenOrderExists_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals(order.getId(), result.get().getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ReturnsEmpty() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(1L);

        assertFalse(result.isPresent());
        verify(orderRepository).findById(1L);
    }

    @Test
    void handleSaveOrder_SavesAndReturnsOrder() {
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.handleSaveOrder(order);

        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        verify(orderRepository).save(order);
    }

    @Test
    void countAllOrders_ReturnsCount() {
        when(orderRepository.count()).thenReturn(5L);

        long result = orderService.countAllOrders();

        assertEquals(5L, result);
        verify(orderRepository).count();
    }

    @Test
    void getListOrderByUser_ReturnsOrders() {
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByUser(user)).thenReturn(orders);

        List<Order> result = orderService.getListOrderByUser(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getId());
        verify(orderRepository).findByUser(user);
    }

    @Test
    void deleteOrderById_WhenOrderExists_DeletesOrderAndDetails() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderDetailRepository).deleteById(orderDetail.getId());
        verify(orderRepository).deleteById(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    void deleteOrderById_WhenOrderDoesNotExist_DoesNotDeleteDetails() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        orderService.deleteOrderById(1L);

        verify(orderRepository).deleteById(1L);
        verify(orderRepository).findById(1L);
        verifyNoInteractions(orderDetailRepository);
    }
}