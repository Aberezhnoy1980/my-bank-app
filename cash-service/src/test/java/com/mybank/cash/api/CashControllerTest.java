package com.mybank.cash.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mybank.cash.service.CashOperationException;
import com.mybank.cash.service.CashService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CashController.class)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashService cashService;

    @Test
    void shouldDepositSuccessfully() throws Exception {
        when(cashService.deposit(eq(new BigDecimal("250.00"))))
                .thenReturn(new CashOperationResponse("DEPOSIT_SUCCESS", new BigDecimal("10250.00")));

        mockMvc.perform(post("/api/cash/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 250.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEPOSIT_SUCCESS"))
                .andExpect(jsonPath("$.balance").value(10250.00));
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        when(cashService.withdraw(eq(new BigDecimal("300.00"))))
                .thenReturn(new CashOperationResponse("WITHDRAW_SUCCESS", new BigDecimal("9700.00")));

        mockMvc.perform(post("/api/cash/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 300.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAW_SUCCESS"))
                .andExpect(jsonPath("$.balance").value(9700.00));
    }

    @Test
    void shouldReturnBadRequestForInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/cash/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("amount: amount must be greater than 0"));
    }

    @Test
    void shouldReturnBadRequestForInsufficientFunds() throws Exception {
        when(cashService.withdraw(eq(new BigDecimal("20000.00"))))
                .thenThrow(new CashOperationException("insufficient funds"));

        mockMvc.perform(post("/api/cash/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 20000.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cash operation failed"))
                .andExpect(jsonPath("$.errors[0]").value("insufficient funds"));
    }
}
