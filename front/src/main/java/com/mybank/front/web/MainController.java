package com.mybank.front.web;

import com.mybank.front.client.AccountProfileView;
import com.mybank.front.client.AccountsGatewayClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final AccountsGatewayClient accountsGatewayClient;

    public MainController(AccountsGatewayClient accountsGatewayClient) {
        this.accountsGatewayClient = accountsGatewayClient;
    }

    @GetMapping("/")
    public String getMainPage(Model model) {
        try {
            AccountProfileView profile = accountsGatewayClient.getCurrentAccount();
            model.addAttribute("account", profile);
            model.addAttribute("hasAccount", true);
        } catch (Exception ex) {
            model.addAttribute("hasAccount", false);
            model.addAttribute("errorMessage", "Accounts service is unavailable now.");
        }
        return "main";
    }
}
