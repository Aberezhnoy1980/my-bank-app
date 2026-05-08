package com.mybank.front.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.mybank.front.client.AccountProfileView;
import com.mybank.front.client.AccountsGatewayClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
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
}
