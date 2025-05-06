package com.computer.demoComputer.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.computer.demoComputer.domain.Role;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.service.UploadService;
import com.computer.demoComputer.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserService userService, 
            UploadService uploadService, 
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/admin/user/create")
    public String createUserPage(Model model) {
        List<Role> allRoles = this.userService.getAllRoles();
        model.addAttribute("newUser", new User());    
        model.addAttribute("roles", allRoles);    
        return "admin/user/createUser";
    }

    @RequestMapping("/admin/user")
    public String tableUserPage(Model model,
                                @RequestParam("page") Optional<String> pageOptional) {

        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {

            }
        } catch (Exception e) {

        }

        Pageable pageable = PageRequest.of(page - 1, 2);
        Page<User> pageHavUsers = this.userService.getAllUsers(pageable);    
        List<User> allUsers = pageHavUsers.getContent();
        List<Role> showRoles = this.userService.getAllRoles();

        model.addAttribute("users", allUsers);
        model.addAttribute("role", showRoles);
        model.addAttribute("currentPage", page);
        model.addAttribute("allPages", pageHavUsers.getTotalPages());

        return "admin/user/tableUser";
    }

    @RequestMapping("/admin/user/{id}")
    public String userDetailsPage(Model model, @PathVariable long id) {
        User showUserById = this.userService.getUserById(id).get(); 
        Role showRoleById = this.userService.getRoleById(id);   
        model.addAttribute("user", showUserById);
        model.addAttribute("role", showRoleById);
        model.addAttribute("id", id);
        return "admin/user/detailUser";
    }

    @RequestMapping("/admin/user/update/{id}")
    public String updateUserPage(Model model, @PathVariable long id) {
        User showUserById = this.userService.getUserById(id).get();    
        List<Role> allRoles = this.userService.getAllRoles();
        model.addAttribute("updateUser", showUserById);
        model.addAttribute("roles", allRoles);
        model.addAttribute("id", id);
        return "admin/user/updateUser";
    }

    @RequestMapping("/admin/user/delete/{id}")
    public String deleteUserPage(Model model, @PathVariable long id) {
        User showUserById = this.userService.getUserById(id).get();    
        model.addAttribute("deleteUser", showUserById);
        model.addAttribute("id", id);
        return "admin/user/deleteUser";
    }

    @RequestMapping(value = "/admin/user/update", method = RequestMethod.POST)
    public String userPageAfterUpdate(
            @ModelAttribute("updateUser") @Valid User user,
            BindingResult updateUserBindingResult, 
            @RequestParam("myFile") MultipartFile multipartFile, 
            Model model) { 

        List<FieldError> errors = updateUserBindingResult.getFieldErrors();
        for(FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + "-" + error.getDefaultMessage());
        }

        User currentUser = this.userService.getUserById(user.getId()).get();
        String avatar = this.uploadService.handleSaveUploadAvatar(multipartFile);
        Role roleChange = this.userService.getRoleById(user.getRole().getId());

        if(updateUserBindingResult.hasErrors()) {
            List<Role> allRoles = this.userService.getAllRoles();
            model.addAttribute("roles", allRoles);
            model.addAttribute("updateUser", user);
            user.setAvatar(currentUser.getAvatar());
            return "admin/user/updateUser";
        }

        if(currentUser != null) {
            currentUser.setPhoneNumber(user.getPhoneNumber());
            currentUser.setFullName(user.getFullName());
            currentUser.setAddress(user.getAddress());
            currentUser.setRole(roleChange);

            if(!multipartFile.isEmpty()) {
                currentUser.setAvatar(avatar);
            }
            this.userService.handleSaveUser(currentUser);   
        }
        return "redirect:/admin/user";
    }

    @PostMapping("/admin/user/create")
    public String userPageAfterCreate(
        @ModelAttribute("newUser") @Valid User user, 
        BindingResult newUserBindingResult, 
        @RequestParam("myFile") MultipartFile multipartFile, 
        Model model) {

        List<FieldError> errors = newUserBindingResult.getFieldErrors();
        for(FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + "-" + error.getDefaultMessage());
        }

        if(newUserBindingResult.hasErrors()) {
            List<Role> allRoles = this.userService.getAllRoles();
            model.addAttribute("roles", allRoles);
            model.addAttribute("newUser", user);
            return "admin/user/createUser";
        }

        String avatar = this.uploadService.handleSaveUploadAvatar(multipartFile);
        String hashPassword= this.passwordEncoder.encode(user.getPassword());
        Role role = this.userService.getRoleById(user.getRole().getId());

        user.setAvatar(avatar);
        user.setPassword(hashPassword);  
        user.setRole(role);

        this.userService.handleSaveUser(user);
        return "redirect:/admin/user";
    }
    
    @RequestMapping(value = "/admin/user/delete", method = RequestMethod.POST)
    public String userPageAfterDelete(@ModelAttribute("deleteUser") User user,
                                      HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        User currentUser = this.userService.getUserById(user.getId()).get();
        if (currentUser != null) {
            this.userService.deleteUserById(user.getId(), session);
        }
        return "redirect:/admin/user";
    }
}
