package com.mybank.accounts.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCurrentAccountProfile() throws Exception {
        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo.user"))
                .andExpect(jsonPath("$.fullName").value("Demo User"))
                .andExpect(jsonPath("$.birthDate").value("1995-05-20"))
                .andExpect(jsonPath("$.balance").value(10000.00));
    }

    @Test
    void shouldUpdateCurrentAccountProfile() throws Exception {
        String payload = """
                {
                  "fullName": "John Smith",
                  "birthDate": "1990-01-10"
                }
                """;

        mockMvc.perform(put("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo.user"))
                .andExpect(jsonPath("$.fullName").value("John Smith"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-10"))
                .andExpect(jsonPath("$.balance").value(10000.00));
    }

    @Test
    void shouldReturnBadRequestForUnderageBirthDate() throws Exception {
        String payload = """
                {
                  "fullName": "John Smith",
                  "birthDate": "2012-01-10"
                }
                """;

        mockMvc.perform(put("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[*]", Matchers.hasItem(Matchers.containsString("birthDate"))));
    }

    @Test
    void shouldReturnBadRequestForBlankFullName() throws Exception {
        String payload = """
                {
                  "fullName": "",
                  "birthDate": "1990-01-10"
                }
                """;

        mockMvc.perform(put("/api/accounts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[*]", Matchers.hasItem(Matchers.containsString("fullName"))));
    }

    @Test
    void shouldDepositToCurrentAccount() throws Exception {
        String payload = """
                {
                  "operationType": "DEPOSIT",
                  "amount": 150.00
                }
                """;

        mockMvc.perform(put("/api/accounts/me/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(10150.00));
    }

    @Test
    void shouldWithdrawFromCurrentAccount() throws Exception {
        String payload = """
                {
                  "operationType": "WITHDRAW",
                  "amount": 300.00
                }
                """;

        mockMvc.perform(put("/api/accounts/me/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(9700.00));
    }

    @Test
    void shouldReturnBadRequestWhenInsufficientFunds() throws Exception {
        String payload = """
                {
                  "operationType": "WITHDRAW",
                  "amount": 20000.00
                }
                """;

        mockMvc.perform(put("/api/accounts/me/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Balance update failed"))
                .andExpect(jsonPath("$.errors[0]").value("insufficient funds"));
    }

    @Test
    void shouldGetAccountByUsername() throws Exception {
        mockMvc.perform(get("/api/accounts/alice.user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice.user"))
                .andExpect(jsonPath("$.fullName").value("Alice User"))
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    void shouldUpdateAccountBalanceByUsername() throws Exception {
        String payload = """
                {
                  "operationType": "DEPOSIT",
                  "amount": 200.00
                }
                """;

        mockMvc.perform(put("/api/accounts/alice.user/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice.user"))
                .andExpect(jsonPath("$.balance").value(5200.00));
    }
}
