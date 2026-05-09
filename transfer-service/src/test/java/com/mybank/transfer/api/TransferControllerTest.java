package com.mybank.transfer.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mybank.transfer.service.TransferOperationException;
import com.mybank.transfer.service.TransferService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    void shouldTransferSuccessfully() throws Exception {
        when(transferService.transfer(any(TransferRequest.class)))
                .thenReturn(new TransferResponse(
                        "TRANSFER_SUCCESS",
                        "demo.user",
                        "alice.user",
                        new BigDecimal("200.00"),
                        new BigDecimal("9800.00"),
                        new BigDecimal("5200.00")
                ));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientUsername": "alice.user",
                                  "amount": 200.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TRANSFER_SUCCESS"))
                .andExpect(jsonPath("$.senderUsername").value("demo.user"))
                .andExpect(jsonPath("$.recipientUsername").value("alice.user"));
    }

    @Test
    void shouldReturnBadRequestForValidationError() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientUsername": "",
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldReturnBadRequestForTransferOperationError() throws Exception {
        when(transferService.transfer(any(TransferRequest.class)))
                .thenThrow(new TransferOperationException("sender and recipient must be different"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientUsername": "demo.user",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transfer failed"))
                .andExpect(jsonPath("$.errors[0]").value("sender and recipient must be different"));
    }
}
