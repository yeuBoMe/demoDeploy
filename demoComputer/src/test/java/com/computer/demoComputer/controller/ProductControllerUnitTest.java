package com.computer.demoComputer.controller;

import com.computer.demoComputer.controller.admin.ProductController;
import com.computer.demoComputer.domain.Product;
import com.computer.demoComputer.service.ProductService;
import com.computer.demoComputer.service.UploadService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(ProductControllerUnitTest.TestResultWatcher.class)
@WebMvcTest(ProductController.class)
public class ProductControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private UploadService uploadService;

    private Product product;

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
        cell.setCellValue("UNIT TEST RESULTS - ProductController");

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
        String filePath = "target/ProductControllerUnitTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/ProductControllerUnitTest.xlsx");
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
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setImage("test.jpg");
        product.setShortDesc("Short description");
        product.setDetailDesc("Detailed description");
        product.setQuantity(10);
        product.setFactory("Test Factory");
        product.setTarget("Test Target");
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProductPage_ReturnsProductPage() throws Exception {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productService.getAllProducts(any())).thenReturn(productPage);

        mockMvc.perform(get("/admin/product")
                .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/tableProduct"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("allPages", 1));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getCreateProductPage_ReturnsCreatePage() throws Exception {
        mockMvc.perform(get("/admin/product/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/createProduct"))
                .andExpect(model().attributeExists("newProduct"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProductDetailPage_ReturnsDetailPage() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/admin/product/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/detailProduct"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("id", 1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProductPageAfterCreate_WithErrors_ReturnsCreatePage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("myFile", "test.jpg", "image/jpeg", new byte[0]);

        // Simulate validation errors by not providing required fields
        mockMvc.perform(multipart("/admin/product/create")
                .file(file)
                .param("name", "") // Invalid name to trigger validation error
                .param("price", "100.0")
                .param("shortDesc", "Short description")
                .param("detailDesc", "Detailed description")
                .param("quantity", "10")
                .param("factory", "Test Factory")
                .param("target", "Test Target")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/createProduct"))
                .andExpect(model().attributeExists("newProduct"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProductPageAfterCreate_Success_RedirectsToProductPage() throws Exception {
        when(uploadService.handleSaveUploadImage(any())).thenReturn("test.jpg");

        MockMultipartFile file = new MockMultipartFile("myFile", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(multipart("/admin/product/create")
                .file(file)
                .param("name", "Test Product")
                .param("price", "100.0")
                .param("shortDesc", "Short description")
                .param("detailDesc", "Detailed description")
                .param("quantity", "10")
                .param("factory", "Test Factory")
                .param("target", "Test Target")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProductPageAfterUpdate_WithErrors_ReturnsUpdatePage() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        MockMultipartFile file = new MockMultipartFile("myFile", "test.jpg", "image/jpeg", new byte[0]);

        // Simulate validation errors by not providing required fields
        mockMvc.perform(multipart("/admin/product/update")
                .file(file)
                .param("id", "1")
                .param("name", "") // Invalid name to trigger validation error
                .param("price", "150.0")
                .param("shortDesc", "Updated short description")
                .param("detailDesc", "Updated detailed description")
                .param("quantity", "20")
                .param("factory", "Updated Factory")
                .param("target", "Updated Target")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product/updateProduct"))
                .andExpect(model().attributeExists("updateProduct"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProductPageAfterUpdate_Success_RedirectsToProductPage() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(uploadService.handleSaveUploadImage(any())).thenReturn("test.jpg");

        MockMultipartFile file = new MockMultipartFile("myFile", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(multipart("/admin/product/update")
                .file(file)
                .param("id", "1")
                .param("name", "Updated Product")
                .param("price", "150.0")
                .param("shortDesc", "Updated short description")
                .param("detailDesc", "Updated detailed description")
                .param("quantity", "20")
                .param("factory", "Updated Factory")
                .param("target", "Updated Target")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/product"));
    }
}