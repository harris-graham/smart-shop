package com.smartshop.controller;

import com.smartshop.dto.CustomerForm;
import com.smartshop.dto.EmployeeForm;
import com.smartshop.entity.Customer;
import com.smartshop.entity.Employee;
import com.smartshop.service.CustomerService;
import com.smartshop.service.EmployeeService;
import com.smartshop.service.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SecurityUtils securityUtils;
    private final CustomerService customerService;
    private final EmployeeService employeeService;

    @GetMapping("/account")
    public String account(Model model) {
        Optional<Customer> customer = securityUtils.currentCustomer();
        if (customer.isPresent()) {
            CustomerForm form = new CustomerForm();
            form.setCustid(customer.get().getCustid());
            form.setCustname(customer.get().getCustname());
            form.setCustpnum(customer.get().getCustpnum());
            form.setCustaddress(customer.get().getCustaddress());
            form.setEmail(customer.get().getEmail());
            model.addAttribute("customerForm", form);
            model.addAttribute("isCustomer", true);
        } else {
            Employee employee = securityUtils.currentEmployee()
                    .orElseThrow(() -> new IllegalStateException("Not signed in"));
            EmployeeForm form = new EmployeeForm();
            form.setEmpid(employee.getEmpid());
            form.setEmpname(employee.getEmpname());
            form.setEmppnum(employee.getEmppnum());
            form.setEmpaddress(employee.getEmpaddress());
            form.setEmpuname(employee.getEmpuname());
            form.setRole(employee.getRole());
            model.addAttribute("employeeForm", form);
            model.addAttribute("isCustomer", false);
        }
        model.addAttribute("displayName", securityUtils.displayName());
        return "account/index";
    }

    @PostMapping("/account/customer")
    public String updateCustomer(@Valid @ModelAttribute("customerForm") CustomerForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isCustomer", true);
            model.addAttribute("displayName", securityUtils.displayName());
            return "account/index";
        }
        Customer customer = securityUtils.currentCustomer().orElseThrow();
        customerService.update(customer.getCustid(), form);
        redirectAttributes.addFlashAttribute("message", "Profile updated");
        return "redirect:/account";
    }

    @PostMapping("/account/employee")
    public String updateEmployee(@RequestParam String empuname,
                                 @RequestParam(required = false) String emppword,
                                 RedirectAttributes redirectAttributes) {
        Employee employee = securityUtils.currentEmployee().orElseThrow();
        try {
            employeeService.updateAccount(employee.getEmpid(), empuname, emppword);
            redirectAttributes.addFlashAttribute("message", "Account updated");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/account";
    }
}
