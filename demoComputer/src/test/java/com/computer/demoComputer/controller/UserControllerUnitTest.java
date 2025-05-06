package com.computer.demoComputer.controller;

import com.computer.demoComputer.controller.admin.UserController;
import com.computer.demoComputer.domain.Role;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.service.UploadService;
import com.computer.demoComputer.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(UserControllerUnitTest.TestResultWatcher.class)
public class UserControllerUnitTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UploadService uploadService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private User user;
    private Role role;
    private List<User> userList;
    private List<Role> roleList;
    private Page<User> userPage;

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
        cell.setCellValue("UNIT TEST RESULTS - UserController");

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
        String filePath = "target/UserControllerUnitTest_" + timestamp + ".xlsx";

        File oldFile = new File("target/UserControllerUnitTest.xlsx");
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
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("password123");
        user.setAddress("123 Street");
        user.setRole(role);

        userList = Collections.singletonList(user);
        roleList = Collections.singletonList(role);
        userPage = new PageImpl<>(userList, PageRequest.of(0, 2), 1);
    }

    @Test
    void testCreateUserPage() {
        when(userService.getAllRoles()).thenReturn(roleList);

        String view = userController.createUserPage(model);

        assertEquals("admin/user/createUser", view);
        verify(model).addAttribute(eq("newUser"), any(User.class));
        verify(model).addAttribute("roles", roleList);
        verify(userService).getAllRoles();
    }

    @Test
    void testTableUserPage_ValidPage() {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);
        when(userService.getAllRoles()).thenReturn(roleList);

        String view = userController.tableUserPage(model, Optional.of("2"));

        assertEquals("admin/user/tableUser", view);
        verify(model).addAttribute("users", userList);
        verify(model).addAttribute("role", roleList);
        verify(model).addAttribute("currentPage", 2);
        verify(model).addAttribute("allPages", 1);
        verify(userService).getAllUsers(any(Pageable.class));
        verify(userService).getAllRoles();
    }

    @Test
    void testTableUserPage_NoPageParam() {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);
        when(userService.getAllRoles()).thenReturn(roleList);

        String view = userController.tableUserPage(model, Optional.empty());

        assertEquals("admin/user/tableUser", view);
        verify(model).addAttribute("users", userList);
        verify(model).addAttribute("role", roleList);
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("allPages", 1);
        verify(userService).getAllUsers(any(Pageable.class));
        verify(userService).getAllRoles();
    }

    @Test
    void testTableUserPage_InvalidPage() {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);
        when(userService.getAllRoles()).thenReturn(roleList);

        String view = userController.tableUserPage(model, Optional.of("invalid"));

        assertEquals("admin/user/tableUser", view);
        verify(model).addAttribute("users", userList);
        verify(model).addAttribute("role", roleList);
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("allPages", 1);
        verify(userService).getAllUsers(any(Pageable.class));
        verify(userService).getAllRoles();
    }

    @Test
    void testUserDetailsPage() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.getRoleById(1L)).thenReturn(role);

        String view = userController.userDetailsPage(model, 1L);

        assertEquals("admin/user/detailUser", view);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("role", role);
        verify(model).addAttribute("id", 1L);
        verify(userService).getUserById(1L);
        verify(userService).getRoleById(1L);
    }

    @Test
    void testUpdateUserPage() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.getAllRoles()).thenReturn(roleList);

        String view = userController.updateUserPage(model, 1L);

        assertEquals("admin/user/updateUser", view);
        verify(model).addAttribute("updateUser", user);
        verify(model).addAttribute("roles", roleList);
        verify(model).addAttribute("id", 1L);
        verify(userService).getUserById(1L);
        verify(userService).getAllRoles();
    }

    @Test
    void testDeleteUserPage() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        String view = userController.deleteUserPage(model, 1L);

        assertEquals("admin/user/deleteUser", view);
        verify(model).addAttribute("deleteUser", user);
        verify(model).addAttribute("id", 1L);
        verify(userService).getUserById(1L);
    }

    @Test
    void testUserPageAfterUpdate_HasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getAllRoles()).thenReturn(roleList);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.getRoleById(1L)).thenReturn(role);
        when(uploadService.handleSaveUploadAvatar(multipartFile)).thenReturn("avatar.png");

        String view = userController.userPageAfterUpdate(user, bindingResult, multipartFile, model);

        assertEquals("admin/user/updateUser", view);
        verify(model).addAttribute("roles", roleList);
        verify(model).addAttribute("updateUser", user);
        verify(userService).getAllRoles();
        verify(userService).getUserById(1L);
        verify(userService).getRoleById(1L);
        verify(uploadService).handleSaveUploadAvatar(multipartFile);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testUserPageAfterUpdate_NoErrors_WithFile() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(uploadService.handleSaveUploadAvatar(multipartFile)).thenReturn("avatar.png");
        when(userService.getRoleById(1L)).thenReturn(role);
        when(multipartFile.isEmpty()).thenReturn(false);

        String view = userController.userPageAfterUpdate(user, bindingResult, multipartFile, model);

        assertEquals("redirect:/admin/user", view);
        verify(userService).getUserById(1L);
        verify(userService).getRoleById(1L);
        verify(userService).handleSaveUser(any(User.class));
        verify(uploadService).handleSaveUploadAvatar(multipartFile);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testUserPageAfterUpdate_NoErrors_NoFile() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.getRoleById(1L)).thenReturn(role);
        when(multipartFile.isEmpty()).thenReturn(true);
        when(uploadService.handleSaveUploadAvatar(multipartFile)).thenReturn("avatar.png");

        String view = userController.userPageAfterUpdate(user, bindingResult, multipartFile, model);

        assertEquals("redirect:/admin/user", view);
        verify(userService).getUserById(1L);
        verify(userService).getRoleById(1L);
        verify(userService).handleSaveUser(any(User.class));
        verify(uploadService).handleSaveUploadAvatar(multipartFile);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testUserPageAfterCreate_HasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getAllRoles()).thenReturn(roleList);

        String view = userController.userPageAfterCreate(user, bindingResult, multipartFile, model);

        assertEquals("admin/user/createUser", view);
        verify(model).addAttribute("roles", roleList);
        verify(model).addAttribute("newUser", user);
        verify(userService).getAllRoles();
        verifyNoInteractions(uploadService, passwordEncoder);
    }

    @Test
    void testUserPageAfterCreate_NoErrors() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(uploadService.handleSaveUploadAvatar(multipartFile)).thenReturn("avatar.png");
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userService.getRoleById(1L)).thenReturn(role);

        String view = userController.userPageAfterCreate(user, bindingResult, multipartFile, model);

        assertEquals("redirect:/admin/user", view);
        verify(userService).getRoleById(1L);
        verify(userService).handleSaveUser(any(User.class));
        verify(uploadService).handleSaveUploadAvatar(multipartFile);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void testUserPageAfterDelete() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(request.getSession(false)).thenReturn(session);

        String view = userController.userPageAfterDelete(user, request);

        assertEquals("redirect:/admin/user", view);
        verify(userService).getUserById(1L);
        verify(userService).deleteUserById(1L, session);
        verify(request).getSession(false);
    }
}