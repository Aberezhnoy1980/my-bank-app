package com.mybank.front.web;

import com.mybank.front.client.AccountUpdateValidationException;
import com.mybank.front.client.AccountProfileView;
import com.mybank.front.client.AccountsGatewayClient;
import com.mybank.front.client.CashGatewayClient;
import com.mybank.front.client.CashOperationResponseView;
import com.mybank.front.client.CashValidationException;
import com.mybank.front.client.TransferGatewayClient;
import com.mybank.front.client.TransferRequest;
import com.mybank.front.client.TransferResponseView;
import com.mybank.front.client.TransferValidationException;
import com.mybank.front.client.UpdateAccountProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final AccountsGatewayClient accountsGatewayClient;
    private final TransferGatewayClient transferGatewayClient;
    private final CashGatewayClient cashGatewayClient;

    public MainController(
            AccountsGatewayClient accountsGatewayClient,
            TransferGatewayClient transferGatewayClient,
            CashGatewayClient cashGatewayClient
    ) {
        this.accountsGatewayClient = accountsGatewayClient;
        this.transferGatewayClient = transferGatewayClient;
        this.cashGatewayClient = cashGatewayClient;
    }

    @GetMapping("/")
    public String getMainPage(Model model) {
        try {
            AccountProfileView profile = accountsGatewayClient.getCurrentAccount();
            model.addAttribute("account", profile);
            model.addAttribute("hasAccount", true);
        } catch (Exception ex) {
            log.warn("Failed to load account profile via Gateway: {}", ex.toString());
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

    @PostMapping("/cash/deposit")
    public String deposit(
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes
    ) {
        return handleCashOperation(amount, redirectAttributes, true);
    }

    @PostMapping("/cash/withdraw")
    public String withdraw(
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes
    ) {
        return handleCashOperation(amount, redirectAttributes, false);
    }

    private String handleCashOperation(BigDecimal amount, RedirectAttributes redirectAttributes, boolean isDeposit) {
        try {
            CashOperationResponseView response = isDeposit
                    ? cashGatewayClient.deposit(amount)
                    : cashGatewayClient.withdraw(amount);
            String action = isDeposit ? "Deposit" : "Withdraw";
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    action + " completed. Current balance: " + response.balance() + "."
            );
        } catch (CashValidationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", String.join("; ", ex.getErrors()));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cash operation failed.");
        }
        return "redirect:/";
    }
}
