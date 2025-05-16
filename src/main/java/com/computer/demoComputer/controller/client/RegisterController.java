package com.computer.demoComputer.controller.client;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.computer.demoComputer.domain.Role;
import com.computer.demoComputer.domain.User;
import com.computer.demoComputer.domain.dto.RegisterDTO;
import com.computer.demoComputer.service.UserService;

import jakarta.validation.Valid;

@Controller
public class RegisterController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {

        model.addAttribute("registerUser", new RegisterDTO());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("registerUser") @Valid RegisterDTO registerDTO,
            BindingResult registerUserBindingResult, Model model) {

        List<FieldError> errors = registerUserBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + "-" + error.getDefaultMessage());
        }

        if (registerUserBindingResult.hasErrors()) {
            model.addAttribute("registerUser", registerDTO);
            return "client/auth/register";
        }

        User user = this.userService.registerDTOtoUser(registerDTO);
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        Role role = this.userService.getRoleById(2);
        user.setPassword(hashPassword);
        user.setRole(role);

        this.userService.handleSaveUser(user);
        return "redirect:/login";
    }
}
