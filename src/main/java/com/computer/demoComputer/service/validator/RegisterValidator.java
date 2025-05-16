package com.computer.demoComputer.service.validator;

import com.computer.demoComputer.domain.dto.RegisterDTO;
import com.computer.demoComputer.service.UserService;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegisterValidator implements ConstraintValidator<RegisterChecked, RegisterDTO> {

    private final UserService userService;

    public RegisterValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(RegisterDTO registerDTO, ConstraintValidatorContext context) {
        boolean valid = true;

        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            context.buildConstraintViolationWithTemplate("Mật khẩu nhập không chính xác!")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();

            valid = false;
        }

        if (this.userService.checkEmailExist(registerDTO.getEmail())) {
            context.buildConstraintViolationWithTemplate("Email đã tồn tại!")
                   .addPropertyNode("email")
                   .addConstraintViolation()
                   .disableDefaultConstraintViolation();

            valid = false;
        }

        return valid;
    }
}