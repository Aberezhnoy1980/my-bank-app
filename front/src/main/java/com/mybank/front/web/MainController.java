package com.mybank.front.web;

import com.mybank.front.client.AccountUpdateValidationException;
import com.mybank.front.client.AccountProfileView;
import com.mybank.front.client.AccountsGatewayClient;
import com.mybank.front.client.TransferGatewayClient;
import com.mybank.front.client.TransferRequest;
import com.mybank.front.client.TransferResponseView;
import com.mybank.front.client.TransferValidationException;
import com.mybank.front.client.UpdateAccountProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private final AccountsGatewayClient accountsGatewayClient;
    private final TransferGatewayClient transferGatewayClient;

    public MainController(
            AccountsGatewayClient accountsGatewayClient,
            TransferGatewayClient transferGatewayClient
    ) {
        this.accountsGatewayClient = accountsGatewayClient;
        this.transferGatewayClient = transferGatewayClient;
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

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("birthDate") LocalDate birthDate,
            RedirectAttributes redirectAttributes
    ) {
        try {
            accountsGatewayClient.updateCurrentAccount(new UpdateAccountProfileRequest(fullName, birthDate));
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (AccountUpdateValidationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", String.join("; ", ex.getErrors()));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Profile update failed.");
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam("recipientUsername") String recipientUsername,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes
    ) {
        try {
            TransferResponseView response = transferGatewayClient.transfer(new TransferRequest(recipientUsername, amount));
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transfer completed: " + response.amount() + " to " + response.recipientUsername() + "."
            );
        } catch (TransferValidationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", String.join("; ", ex.getErrors()));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transfer failed.");
        }
        return "redirect:/";
    }
}
