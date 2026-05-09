package com.mybank.transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mybank.transfer.api.TransferRequest;
import com.mybank.transfer.api.TransferResponse;
import com.mybank.transfer.client.AccountProfileView;
import com.mybank.transfer.client.AccountsClient;
import com.mybank.transfer.client.NotificationsClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransferServiceTest {

    private AccountsClient accountsClient;
    private NotificationsClient notificationsClient;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        accountsClient = Mockito.mock(AccountsClient.class);
        notificationsClient = Mockito.mock(NotificationsClient.class);
        transferService = new TransferService(accountsClient, notificationsClient, "demo.user");
    }

    @Test
    void shouldTransferSuccessfully() {
        TransferRequest request = new TransferRequest("alice.user", new BigDecimal("200.00"));

        when(accountsClient.getByUsername("alice.user")).thenReturn(profile("alice.user", "5000.00"));
        when(accountsClient.withdraw("demo.user", new BigDecimal("200.00"))).thenReturn(profile("demo.user", "9800.00"));
        when(accountsClient.deposit("alice.user", new BigDecimal("200.00"))).thenReturn(profile("alice.user", "5200.00"));

        TransferResponse response = transferService.transfer(request);

        assertEquals("TRANSFER_SUCCESS", response.status());
        assertEquals(new BigDecimal("9800.00"), response.senderBalance());
        assertEquals(new BigDecimal("5200.00"), response.recipientBalance());
    }

    @Test
    void shouldRejectSelfTransfer() {
        TransferRequest request = new TransferRequest("demo.user", new BigDecimal("100.00"));

        assertThrows(TransferOperationException.class, () -> transferService.transfer(request));
    }

    @Test
    void shouldCompensateOnRecipientDepositFailure() {
        TransferRequest request = new TransferRequest("alice.user", new BigDecimal("100.00"));

        when(accountsClient.getByUsername("alice.user")).thenReturn(profile("alice.user", "5000.00"));
        when(accountsClient.withdraw("demo.user", new BigDecimal("100.00"))).thenReturn(profile("demo.user", "9900.00"));
        when(accountsClient.deposit("alice.user", new BigDecimal("100.00")))
                .thenThrow(new TransferOperationException("recipient update failed"));
        when(accountsClient.deposit("demo.user", new BigDecimal("100.00"))).thenReturn(profile("demo.user", "10000.00"));

        assertThrows(TransferOperationException.class, () -> transferService.transfer(request));

        verify(accountsClient).deposit("demo.user", new BigDecimal("100.00"));
    }

    private AccountProfileView profile(String username, String balance) {
        return new AccountProfileView(
                username,
                "Any User",
                LocalDate.of(1990, 1, 1),
                new BigDecimal(balance)
        );
    }
}
