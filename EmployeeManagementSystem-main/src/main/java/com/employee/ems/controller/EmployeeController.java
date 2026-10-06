package com.employee.ems.controller;

import com.employee.ems.model.Employee;
import com.employee.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }

    @GetMapping
    public String listEmployees(Model model, Authentication authentication) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("isAdmin", isAdmin(authentication));
        model.addAttribute("username", authentication != null ? authentication.getName() : null);
        return "employees/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/employees";
        }
        model.addAttribute("employee", new Employee());
        model.addAttribute("isAdmin", true);
        model.addAttribute("username", authentication.getName());
        return "employees/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/employees";
        }
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        model.addAttribute("isAdmin", true);
        model.addAttribute("username", authentication.getName());
        return "employees/form";
    }

    @PostMapping("/save")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                                BindingResult result,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(authentication)) {
            return "redirect:/employees";
        }
        if (result.hasErrors()) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("username", authentication.getName());
            return "employees/form";
        }
        employeeService.saveEmployee(employee);
        redirectAttributes.addFlashAttribute("message", "Employee saved successfully.");
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(authentication)) {
            return "redirect:/employees";
        }
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("message", "Employee deleted successfully.");
        return "redirect:/employees";
    }
}
