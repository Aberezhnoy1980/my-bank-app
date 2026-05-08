package com.mybank.front.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mybank.front.client.AccountProfileView;
import com.mybank.front.client.AccountsGatewayClient;
import com.mybank.front.client.UpdateAccountProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsGatewayClient accountsGatewayClient;

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
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldRedirectWithErrorWhenUpdateFails() throws Exception {
        when(accountsGatewayClient.updateCurrentAccount(ArgumentMatchers.any(UpdateAccountProfileRequest.class)))
                .thenThrow(new RuntimeException("validation failed"));

        mockMvc.perform(post("/profile")
                        .param("fullName", "")
                        .param("birthDate", "2012-01-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
