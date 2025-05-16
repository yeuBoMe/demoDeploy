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

public class RegisterUITest {

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
        try {
            System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
            driver = new ChromeDriver();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15)); // Tăng thời gian chờ
            testResults.clear();
        } catch (Exception e) {
            System.out.println("Failed to setup WebDriver: " + e.getMessage());
        }
    }

    @BeforeEach
    public void navigateToRegisterPage() {
        try {
            driver.get("http://localhost:8080/register");
            System.out.println("Navigated to register page: " + driver.getCurrentUrl());
        } catch (Exception e) {
            System.out.println("Failed to navigate to register page: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Case 1: Register with valid credentials")
    public void testRegisterWithValidCredentials() {
        System.out.println("Starting Test Case 1: Register with valid credentials...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("John");
            lastName.sendKeys("Doe");
            email.sendKeys("john.doe.new@example.com"); // Sử dụng email mới
            password.sendKeys("123456");
            confirmPassword.sendKeys("123456");
            System.out.println("Submitted registration with valid credentials");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.urlContains("/login"));
            System.out.println("Successfully redirected to: " + driver.getCurrentUrl());
            testResults.add(new TestResult("Test Case 1", "Register with valid credentials", "PASSED", "Successfully registered and redirected to /login"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 1", "Register with valid credentials", "FAILED", "Failed to redirect. Current URL: " + driver.getCurrentUrl() + "; Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 2: Register with existing email")
    public void testRegisterWithExistingEmail() {
        System.out.println("Starting Test Case 2: Register with existing email...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("Jane");
            lastName.sendKeys("Doe");
            email.sendKeys("ninh2k4@gmail.com"); // Email đã tồn tại
            password.sendKeys("123456");
            confirmPassword.sendKeys("123456");
            System.out.println("Submitted registration with existing email");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("Email đã tồn tại!"), "Error message for existing email not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 2", "Register with existing email", "PASSED", "Correct error message displayed for existing email"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 2", "Register with existing email", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 3: Register with mismatched passwords")
    public void testRegisterWithMismatchedPasswords() {
        System.out.println("Starting Test Case 3: Register with mismatched passwords...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("Jane");
            lastName.sendKeys("Doe");
            email.sendKeys("jane.doe@example.com");
            password.sendKeys("123456");
            confirmPassword.sendKeys("654321");
            System.out.println("Submitted registration with mismatched passwords");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("Mật khẩu nhập không chính xác!"), "Error message for mismatched passwords not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 3", "Register with mismatched passwords", "PASSED", "Correct error message displayed for mismatched passwords"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 3", "Register with mismatched passwords", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 4: Register with empty email")
    public void testRegisterWithEmptyEmail() {
        System.out.println("Starting Test Case 4: Register with empty email...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("Jane");
            lastName.sendKeys("Doe");
            email.sendKeys("");
            password.sendKeys("123456");
            confirmPassword.sendKeys("123456");
            System.out.println("Submitted registration with empty email");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("Email không hợp lệ"), "Error message for empty email not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 4", "Register with empty email", "PASSED", "Correct error message displayed for empty email"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 4", "Register with empty email", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 5: Register with empty first name")
    public void testRegisterWithEmptyFirstName() {
        System.out.println("Starting Test Case 5: Register with empty first name...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("");
            lastName.sendKeys("Doe");
            email.sendKeys("jane.doe1@example.com");
            password.sendKeys("123456");
            confirmPassword.sendKeys("123456");
            System.out.println("Submitted registration with empty first name");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("firstName tối thiểu 5 ký tự"), "Error message for empty first name not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 5", "Register with empty first name", "PASSED", "Correct error message displayed for empty first name"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 5", "Register with empty first name", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 6: Register with empty last name")
    public void testRegisterWithEmptyLastName() {
        System.out.println("Starting Test Case 6: Register with empty last name...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("Jane");
            lastName.sendKeys("");
            email.sendKeys("jane.doe2@example.com");
            password.sendKeys("123456");
            confirmPassword.sendKeys("123456");
            System.out.println("Submitted registration with empty last name");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("Họ tên phải tối thiểu 5 ký tự!"), "Error message for empty last name not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 6", "Register with empty last name", "PASSED", "Correct error message displayed for empty last name"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 6", "Register with empty last name", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 7: Register with empty password")
    public void testRegisterWithEmptyPassword() {
        System.out.println("Starting Test Case 7: Register with empty password...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("Jane");
            lastName.sendKeys("Doe");
            email.sendKeys("jane.doe3@example.com");
            password.sendKeys("");
            confirmPassword.sendKeys("");
            System.out.println("Submitted registration with empty password");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("Mật khẩu phải tối thiểu 5 kí tự!"), "Error message for empty password not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 7", "Register with empty password", "PASSED", "Correct error message displayed for empty password"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 7", "Register with empty password", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test Case 8: Register with invalid email format")
    public void testRegisterWithInvalidEmailFormat() {
        System.out.println("Starting Test Case 8: Register with invalid email format...");
        WebElement firstName = null;
        WebElement lastName = null;
        WebElement email = null;
        WebElement password = null;
        WebElement confirmPassword = null;
        WebElement registerButton = null;

        try {
            firstName = driver.findElement(By.id("inputFirstName"));
            lastName = driver.findElement(By.id("inputLastName"));
            email = driver.findElement(By.id("inputEmail"));
            password = driver.findElement(By.id("inputPassword"));
            confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            registerButton = driver.findElement(By.className("btn-register"));

            firstName.sendKeys("Jane");
            lastName.sendKeys("Doe");
            email.sendKeys("invalid-email");
            password.sendKeys("123456");
            confirmPassword.sendKeys("123456");
            System.out.println("Submitted registration with invalid email format");
            registerButton.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'invalid-feedback')]")));
            assertTrue(errorMessage.getText().contains("Email không hợp lệ"), "Error message for invalid email format not displayed");
            System.out.println("Error message found: " + errorMessage.getText());
            testResults.add(new TestResult("Test Case 8", "Register with invalid email format", "PASSED", "Correct error message displayed for invalid email format"));
        } catch (Exception e) {
            System.out.println("Exception details: " + e.getMessage());
            testResults.add(new TestResult("Test Case 8", "Register with invalid email format", "FAILED", "Exception: " + e.getMessage()));
        }
    }

    @AfterEach
    public void clearInputsAndWriteResults() {
        try {
            WebElement firstName = driver.findElement(By.id("inputFirstName"));
            WebElement lastName = driver.findElement(By.id("inputLastName"));
            WebElement email = driver.findElement(By.id("inputEmail"));
            WebElement password = driver.findElement(By.id("inputPassword"));
            WebElement confirmPassword = driver.findElement(By.id("inputPasswordConfirm"));
            firstName.clear();
            lastName.clear();
            email.clear();
            password.clear();
            confirmPassword.clear();
            System.out.println("Cleared input fields");
        } catch (Exception e) {
            System.out.println("Failed to clear inputs: " + e.getMessage());
        }

        // Ghi kết quả sau mỗi test case
        writeResultsToExcel();
    }

    @AfterAll
    public static void tearDown() {
        // Ghi kết quả lần cuối (nếu cần)
        writeResultsToExcel();

        // Đóng trình duyệt
        if (driver != null) {
            driver.quit();
            System.out.println("Browser session closed");
        }
    }

    private static synchronized void writeResultsToExcel() {
        if (testResults.isEmpty()) {
            System.out.println("No test results to write to Excel.");
            return;
        }

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