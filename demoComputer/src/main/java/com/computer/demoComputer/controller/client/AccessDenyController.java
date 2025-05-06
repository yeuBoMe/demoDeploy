package com.computer.demoComputer.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDenyController {
    
    @GetMapping("/access-deny")
    public String getAccessDenyPage() {
        return "client/auth/accessDeny";
    }
}
