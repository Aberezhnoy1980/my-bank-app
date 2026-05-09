package com.mybank.transfer.service;

import com.mybank.transfer.api.TransferRequest;
import com.mybank.transfer.api.TransferResponse;
import com.mybank.transfer.client.AccountProfileView;
import com.mybank.transfer.client.AccountsClient;
import com.mybank.transfer.client.NotificationsClient;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final String senderUsername;

    public TransferService(
            AccountsClient accountsClient,
            NotificationsClient notificationsClient,
            @Value("${app.transfer.sender-username}") String senderUsername
    ) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.senderUsername = senderUsername;
    }

    public TransferResponse transfer(TransferRequest request) {
        validateParticipants(request.recipientUsername());

        // Validate recipient existence before balance changes.
        accountsClient.getByUsername(request.recipientUsername());

        AccountProfileView senderAfterWithdraw = accountsClient.withdraw(senderUsername, request.amount());
        try {
            AccountProfileView recipientAfterDeposit = accountsClient.deposit(request.recipientUsername(), request.amount());
            return new TransferResponse(
                    "TRANSFER_SUCCESS",
                    senderUsername,
                    request.recipientUsername(),
                    request.amount(),
                    senderAfterWithdraw.balance(),
                    recipientAfterDeposit.balance()
            );
            
        } catch (RuntimeException ex) {
            // Best-effort compensation for partial failure.
            accountsClient.deposit(senderUsername, request.amount());
            throw ex;
        } finally {
            notificationsClient.send(
                    "TRANSFER_ATTEMPT",
                    "Transfer attempt from " + senderUsername + " to " + request.recipientUsername()
            );
        }
    }

    private void validateParticipants(String recipientUsername) {
        if (Objects.equals(senderUsername, recipientUsername)) {
            throw new TransferOperationException("sender and recipient must be different");
        }
    }
}
