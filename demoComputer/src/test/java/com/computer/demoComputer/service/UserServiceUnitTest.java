package com.computer.demoComputer.service;

import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.domain.dto.RegisterDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(UserServiceUnitTest.TestResultWatcher.class)
public class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    private static Workbook workbook;
    private static Sheet sheet;
    private static List<TestResult> testResults = new ArrayList<>();
    private static int passCount = 0;
    private static int failCount = 0;
    private static int skippedCount = 0;
    private long startTime;

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
        cell.setCellValue("UNIT TEST RESULTS - UserService");

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    @BeforeEach
    void recordStartTime() {
        startTime = System.currentTimeMillis();
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
        String filePath = "target/UserServiceUnitTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/UserServiceUnitTest.xlsx");
        if (oldFile.exists()) {
            oldFile.delete();
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    @Test
    void testRegisterDTOtoUser_FullData() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("secret");

        User user = userService.registerDTOtoUser(dto);

        assertEquals("John Doe", user.getFullName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertNull(user.getAddress());
        assertNull(user.getPhoneNumber());
    }

    @Test
    void testRegisterDTOtoUser_NullFields() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFirstName(null);
        dto.setLastName(null);
        dto.setEmail(null);
        dto.setPassword(null);

        User user = userService.registerDTOtoUser(dto);

        assertEquals("null null", user.getFullName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getAddress());
        assertNull(user.getPhoneNumber());
    }

    @Test
    void testRegisterDTOtoUser_EmptyFields() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFirstName("");
        dto.setLastName("");
        dto.setEmail("");
        dto.setPassword("");

        User user = userService.registerDTOtoUser(dto);

        assertEquals(" ", user.getFullName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertNull(user.getAddress());
        assertNull(user.getPhoneNumber());
    }

    @Test
    void testRegisterDTOtoUser_PartialData() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFirstName("John");
        dto.setLastName(null);
        dto.setEmail("john@example.com");
        dto.setPassword("");

        User user = userService.registerDTOtoUser(dto);

        assertEquals("John null", user.getFullName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("", user.getPassword());
        assertNull(user.getAddress());
        assertNull(user.getPhoneNumber());
    }

    @Test
    void testRegisterDTOtoUser_InvalidEmail() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setEmail("invalid-email");
        dto.setPassword("password123");

        User user = userService.registerDTOtoUser(dto);

        assertEquals("Jane Doe", user.getFullName());
        assertEquals("invalid-email", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertNull(user.getAddress());
        assertNull(user.getPhoneNumber());
    }
}