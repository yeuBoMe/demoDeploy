package com.computer.demoComputer.controller;

import com.computer.demoComputer.domain.Role;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.repository.RoleRepository;
import com.computer.demoComputer.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(UserControllerIntegrationTest.TestResultWatcher.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;
    private Role role;
    private MockHttpSession session;

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
        cell.setCellValue("INTEGRATION TEST RESULTS - UserController");

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
        String filePath = "target/UserControllerIntegrationTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/UserControllerIntegrationTest.xlsx");
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
        session = new MockHttpSession();

        user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password123");
        user.setAddress("123 Street");

        role = new Role();
        role.setName("ROLE_USER");
        role = roleRepository.save(role);

        user.setRole(role);
        user = userRepository.save(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUserPage() throws Exception {
        mockMvc.perform(get("/admin/user/create")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/createUser"))
                .andExpect(model().attributeExists("newUser"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testTableUserPage() throws Exception {
        mockMvc.perform(get("/admin/user")
                .param("page", "1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/tableUser"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("role"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attributeExists("allPages"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUserDetailsPage() throws Exception {
        mockMvc.perform(get("/admin/user/" + user.getId())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/detailUser"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("id", user.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserPage() throws Exception {
        mockMvc.perform(get("/admin/user/update/" + user.getId())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/updateUser"))
                .andExpect(model().attributeExists("updateUser"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attribute("id", user.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserPage() throws Exception {
        mockMvc.perform(get("/admin/user/delete/" + user.getId())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/deleteUser"))
                .andExpect(model().attributeExists("deleteUser"))
                .andExpect(model().attribute("id", user.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUserPageAfterCreate_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("myFile", "avatar.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/user/create")
                .file(file)
                .param("email", "new@example.com")
                .param("fullName", "New User")
                .param("password", "password123")
                .param("address", "456 Street")
                .param("role.id", String.valueOf(role.getId()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user"));

        User savedUser = userRepository.findByEmail("new@example.com");
        assertNotNull(savedUser);
        assertEquals("New User", savedUser.getFullName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUserPageAfterCreate_ValidationError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("myFile", "avatar.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/user/create")
                .file(file)
                .param("email", "invalid")
                .param("fullName", "New")
                .param("password", "123")
                .param("address", "123")
                .param("role.id", String.valueOf(role.getId()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/createUser"))
                .andExpect(model().attributeExists("newUser"))
                .andExpect(model().attributeExists("roles"));

        User savedUser = userRepository.findByEmail("invalid");
        assertFalse(savedUser != null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUserPageAfterUpdate_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("myFile", "avatar.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/user/update")
                .file(file)
                .param("id", String.valueOf(user.getId()))
                .param("email", "updated@example.com")
                .param("fullName", "Updated User")
                .param("address", "789 Street")
                .param("role.id", String.valueOf(role.getId()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user"));

        Optional<User> updatedUser = userRepository.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Updated User", updatedUser.get().getFullName());
        assertEquals("789 Street", updatedUser.get().getAddress());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUserPageAfterUpdate_ValidationError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("myFile", "avatar.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/user/update")
                .file(file)
                .param("id", String.valueOf(user.getId()))
                .param("email", "invalid")
                .param("fullName", "Up")
                .param("address", "12")
                .param("role.id", String.valueOf(role.getId()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user/updateUser"))
                .andExpect(model().attributeExists("updateUser"))
                .andExpect(model().attributeExists("roles"));

        Optional<User> updatedUser = userRepository.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Test User", updatedUser.get().getFullName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUserPageAfterDelete() throws Exception {
        mockMvc.perform(post("/admin/user/delete")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", String.valueOf(user.getId()))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/user"));

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertFalse(deletedUser.isPresent());
    }
}