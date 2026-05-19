package com.mybank.front.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MainController.class)
@AutoConfigureMockMvc(addFilters = false)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsGatewayClient accountsGatewayClient;

    @MockBean
    private TransferGatewayClient transferGatewayClient;

    @MockBean
    private CashGatewayClient cashGatewayClient;

    @Test
    void shouldRenderMainPageWithAccountData() throws Exception {
        when(accountsGatewayClient.getCurrentAccount()).thenReturn(
                new AccountProfileView(
                        "demo.user",
                        "Demo User",
                        LocalDate.of(1995, 5, 20),
                        new BigDecimal("10000.00")
                )
        );

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("hasAccount", true))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    void shouldRenderFallbackWhenAccountsUnavailable() throws Exception {
        when(accountsGatewayClient.getCurrentAccount()).thenThrow(new RuntimeException("downstream error"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("hasAccount", false))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void shouldUpdateProfileAndRedirectToMainPage() throws Exception {
        when(accountsGatewayClient.updateCurrentAccount(ArgumentMatchers.any(UpdateAccountProfileRequest.class)))
                .thenReturn(new AccountProfileView(
                        "demo.user",
                        "John Smith",
                        LocalDate.of(1990, 1, 10),
                        new BigDecimal("10000.00")
                ));

        mockMvc.perform(post("/profile")
                        .param("fullName", "John Smith")
                        .param("birthDate", "1990-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void shouldRedirectWithValidationErrorDetailsWhenUpdateFails() throws Exception {
        when(accountsGatewayClient.updateCurrentAccount(ArgumentMatchers.any(UpdateAccountProfileRequest.class)))
                .thenThrow(new AccountUpdateValidationException(
                        java.util.List.of("birthDate: age must be 18+", "fullName: must not be blank")
                ));

        mockMvc.perform(post("/profile")
                        .param("fullName", "")
                        .param("birthDate", "2012-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "birthDate: age must be 18+; fullName: must not be blank"));
    }

    @Test
    void shouldRedirectWithGenericErrorWhenUpdateFailsUnexpectedly() throws Exception {
        when(accountsGatewayClient.updateCurrentAccount(ArgumentMatchers.any(UpdateAccountProfileRequest.class)))
                .thenThrow(new RuntimeException("validation failed"));

        mockMvc.perform(post("/profile")
                        .param("fullName", "")
                        .param("birthDate", "2012-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "Profile update failed."));
    }

    @Test
    void shouldTransferAndRedirectToMainPage() throws Exception {
        when(transferGatewayClient.transfer(ArgumentMatchers.any(TransferRequest.class)))
                .thenReturn(new TransferResponseView(
                        "TRANSFER_SUCCESS",
                        "demo.user",
                        "alice.user",
                        new BigDecimal("150.00"),
                        new BigDecimal("9850.00"),
                        new BigDecimal("5150.00")
                ));

        mockMvc.perform(post("/transfer")
                        .param("recipientUsername", "alice.user")
                        .param("amount", "150.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void shouldRedirectWithTransferValidationError() throws Exception {
        when(transferGatewayClient.transfer(ArgumentMatchers.any(TransferRequest.class)))
                .thenThrow(new TransferValidationException(java.util.List.of("insufficient funds")));

        mockMvc.perform(post("/transfer")
                        .param("recipientUsername", "alice.user")
                        .param("amount", "20000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "insufficient funds"));
    }

    @Test
    void shouldRedirectWithTransferGenericError() throws Exception {
        when(transferGatewayClient.transfer(ArgumentMatchers.any(TransferRequest.class)))
                .thenThrow(new RuntimeException("gateway timeout"));

        mockMvc.perform(post("/transfer")
                        .param("recipientUsername", "alice.user")
                        .param("amount", "150.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "Transfer failed."));
    }

    @Test
    void shouldDepositAndRedirectToMainPage() throws Exception {
        when(cashGatewayClient.deposit(new BigDecimal("250.00")))
                .thenReturn(new CashOperationResponseView("DEPOSIT_SUCCESS", new BigDecimal("10250.00")));

        mockMvc.perform(post("/cash/deposit")
                        .param("amount", "250.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void shouldWithdrawAndRedirectToMainPage() throws Exception {
        when(cashGatewayClient.withdraw(new BigDecimal("100.00")))
                .thenReturn(new CashOperationResponseView("WITHDRAW_SUCCESS", new BigDecimal("9900.00")));

        mockMvc.perform(post("/cash/withdraw")
                        .param("amount", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void shouldRedirectWithCashValidationError() throws Exception {
        when(cashGatewayClient.withdraw(new BigDecimal("20000.00")))
                .thenThrow(new CashValidationException(java.util.List.of("insufficient funds")));

        mockMvc.perform(post("/cash/withdraw")
                        .param("amount", "20000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "insufficient funds"));
    }

    @Test
    void shouldRedirectWithCashGenericError() throws Exception {
        when(cashGatewayClient.deposit(new BigDecimal("10.00")))
                .thenThrow(new RuntimeException("gateway timeout"));

        mockMvc.perform(post("/cash/deposit")
                        .param("amount", "10.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "Cash operation failed."));
    }
}
