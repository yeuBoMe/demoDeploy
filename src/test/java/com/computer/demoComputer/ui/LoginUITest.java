package com.computer.demoComputer.ui;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class LoginUITest {

    private static WebDriver driver;
    private static List<TestResult> testResults = new ArrayList<>();

    // Class để lưu kết quả test case
    private static class TestResult {
        String testCase;
        String description;
        String status;
        String details;

        TestResult(String testCase, String description, String status, String details) {
            this.testCase = testCase;
            this.description = description;
            this.status = status;
            this.details = details;
        }
    }

    @BeforeAll
    public static void setup() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        testResults.clear(); // Xóa kết quả cũ nếu có
    }

    @BeforeEach
    public void navigateToLoginPage() {
        driver.get("http://localhost:8080/login");
        System.out.println("Navigated to login page: " + driver.getCurrentUrl());
    }

    @Test
    @DisplayName("Test Case 1: Login with valid credentials (ROLE_ADMIN)")
    public void testLoginWithValidCredentials() {
        System.out.println("Starting Test Case 1: Login with valid credentials (ROLE_ADMIN)...");
        WebElement email = driver.findElement(By.id("inputEmail"));
        WebElement password = driver.findElement(By.id("inputPassword"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        email.sendKeys("ninh2k4@gmail.com");
        password.sendKeys("123456");
        System.out.println("Submitted login with email: ninh2k4@gmail.com, password: 123456");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.urlContains("/admin"));
            System.out.println("Successfully redirected to: " + driver.getCurrentUrl());
            WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            assertTrue(welcomeMessage.getText().contains("Admin") || welcomeMessage.getText().contains("yeu bome"),
                    "Trang sau đăng nhập không chứa 'Admin' hoặc 'yeu bome'");
            testResults.add(new TestResult("Test Case 1", "Login with valid credentials (ROLE_ADMIN)", "PASSED", "Successfully logged in and redirected to /admin"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 1", "Login with valid credentials (ROLE_ADMIN)", "FAILED", "Failed to redirect. Current URL: " + driver.getCurrentUrl() + "; Exception: " + e.getMessage()));
            throw e;
        }
    }

    @Test
    @DisplayName("Test Case 2: Login with invalid credentials")
    public void testLoginWithInvalidCredentials() {
        System.out.println("Starting Test Case 2: Login with invalid credentials...");
        WebElement email = driver.findElement(By.id("inputEmail"));
        WebElement password = driver.findElement(By.id("inputPassword"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        email.sendKeys("wrong@example.com");
        password.sendKeys("wrongpassword");
        System.out.println("Submitted login with email: wrong@example.com, password: wrongpassword");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
            assertTrue(errorMessage.isDisplayed(), "Thông báo lỗi không hiển thị");
            assertTrue(errorMessage.getText().contains("Email hoặc mật khẩu không hợp lệ!"),
                    "Nội dung thông báo lỗi không đúng");
            testResults.add(new TestResult("Test Case 2", "Login with invalid credentials", "PASSED", "Correct error message displayed for invalid credentials"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 2", "Login with invalid credentials", "FAILED", "Exception: " + e.getMessage()));
            throw e;
        }
    }

    @Test
    @DisplayName("Test Case 3: Login with empty email")
    public void testLoginWithEmptyEmail() {
        System.out.println("Starting Test Case 3: Login with empty email...");
        WebElement email = driver.findElement(By.id("inputEmail"));
        WebElement password = driver.findElement(By.id("inputPassword"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        email.sendKeys("");
        password.sendKeys("123456");
        System.out.println("Submitted login with empty email, password: 123456");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
            assertTrue(errorMessage.isDisplayed(), "Thông báo lỗi không hiển thị");
            assertTrue(errorMessage.getText().contains("Email hoặc mật khẩu không hợp lệ!") || 
                       errorMessage.getText().contains("Email không được để trống!"),
                    "Nội dung thông báo lỗi không đúng");
            testResults.add(new TestResult("Test Case 3", "Login with empty email", "PASSED", "Correct error message displayed for empty email"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 3", "Login with empty email", "FAILED", "Exception: " + e.getMessage()));
            throw e;
        }
    }

    @Test
    @DisplayName("Test Case 4: Login with empty password")
    public void testLoginWithEmptyPassword() {
        System.out.println("Starting Test Case 4: Login with empty password...");
        WebElement email = driver.findElement(By.id("inputEmail"));
        WebElement password = driver.findElement(By.id("inputPassword"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        email.sendKeys("ninh2k4@gmail.com");
        password.sendKeys("");
        System.out.println("Submitted login with email: ninh2k4@gmail.com, empty password");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
            assertTrue(errorMessage.isDisplayed(), "Thông báo lỗi không hiển thị");
            assertTrue(errorMessage.getText().contains("Email hoặc mật khẩu không hợp lệ!") || 
                       errorMessage.getText().contains("Mật khẩu không được để trống!"),
                    "Nội dung thông báo lỗi không đúng");
            testResults.add(new TestResult("Test Case 4", "Login with empty password", "PASSED", "Correct error message displayed for empty password"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 4", "Login with empty password", "FAILED", "Exception: " + e.getMessage()));
            throw e;
        }
    }

    @Test
    @DisplayName("Test Case 5: Login with invalid email format")
    public void testLoginWithInvalidEmailFormat() {
        System.out.println("Starting Test Case 5: Login with invalid email format...");
        WebElement email = driver.findElement(By.id("inputEmail"));
        WebElement password = driver.findElement(By.id("inputPassword"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        email.sendKeys("invalid-email");
        password.sendKeys("123456");
        System.out.println("Submitted login with email: invalid-email, password: 123456");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
            assertTrue(errorMessage.isDisplayed(), "Thông báo lỗi không hiển thị");
            assertTrue(errorMessage.getText().contains("Email hoặc mật khẩu không hợp lệ!") || 
                       errorMessage.getText().contains("Email không hợp lệ!"),
                    "Nội dung thông báo lỗi không đúng");
            testResults.add(new TestResult("Test Case 5", "Login with invalid email format", "PASSED", "Correct error message displayed for invalid email format"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 5", "Login with invalid email format", "FAILED", "Exception: " + e.getMessage()));
            throw e;
        }
    }

    @Test
    @DisplayName("Test Case 6: Login with valid email but wrong password")
    public void testLoginWithWrongPassword() {
        System.out.println("Starting Test Case 6: Login with valid email but wrong password...");
        WebElement email = driver.findElement(By.id("inputEmail"));
        WebElement password = driver.findElement(By.id("inputPassword"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        email.sendKeys("ninh2k4@gmail.com");
        password.sendKeys("wrongpassword");
        System.out.println("Submitted login with email: ninh2k4@gmail.com, password: wrongpassword");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-message")));
            assertTrue(errorMessage.isDisplayed(), "Thông báo lỗi không hiển thị");
            assertTrue(errorMessage.getText().contains("Email hoặc mật khẩu không hợp lệ!"),
                    "Nội dung thông báo lỗi không đúng");
            testResults.add(new TestResult("Test Case 6", "Login with valid email but wrong password", "PASSED", "Correct error message displayed for wrong password"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 6", "Login with valid email but wrong password", "FAILED", "Exception: " + e.getMessage()));
            throw e;
        }
    }

    @Test
    @DisplayName("Test Case 7: Logout message")
    public void testLogoutMessage() {
        System.out.println("Starting Test Case 7: Logout message...");
        driver.get("http://localhost:8080/login?logout");
        System.out.println("Navigated to logout page: " + driver.getCurrentUrl());

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("success-message")));
            assertTrue(successMessage.isDisplayed(), "Thông báo đăng xuất không hiển thị");
            assertTrue(successMessage.getText().contains("Bạn đã đăng xuất thành công!"),
                    "Nội dung thông báo đăng xuất không đúng");
            testResults.add(new TestResult("Test Case 7", "Logout message", "PASSED", "Correct logout message displayed"));
        } catch (Exception e) {
            testResults.add(new TestResult("Test Case 7", "Logout message", "FAILED", "Exception: " + e.getMessage()));
            throw e;
        }
    }

    @AfterEach
    public void clearInputs() {
        try {
            WebElement email = driver.findElement(By.id("inputEmail"));
            WebElement password = driver.findElement(By.id("inputPassword"));
            email.clear();
            password.clear();
            System.out.println("Cleared input fields");
        } catch (Exception e) {
            System.out.println("Failed to clear inputs: " + e.getMessage());
        }
    }

    @AfterAll
    public static void tearDown() {
        // Ghi kết quả vào file Excel
        writeResultsToExcel();

        // Đóng trình duyệt
        if (driver != null) {
            driver.quit();
            System.out.println("Browser session closed");
        }
    }

    private static void writeResultsToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Results");

            // Tạo header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Test Case", "Description", "Status", "Details"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Ghi dữ liệu
            for (int i = 0; i < testResults.size(); i++) {
                TestResult result = testResults.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(result.testCase);
                row.createCell(1).setCellValue(result.description);
                row.createCell(2).setCellValue(result.status);
                row.createCell(3).setCellValue(result.details);
            }

            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Đảm bảo thư mục target tồn tại
            File targetDir = new File("target");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
                System.out.println("Created target directory: " + targetDir.getAbsolutePath());
            }

            // Ghi vào file trong thư mục target
            String excelFilePath = "target/TestResults.xlsx";
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
                System.out.println("Test results written to " + excelFilePath);
            }
        } catch (IOException e) {
            System.out.println("Failed to write test results to Excel: " + e.getMessage());
        }
    }
}