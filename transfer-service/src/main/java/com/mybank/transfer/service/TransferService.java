package com.mybank.transfer.service;

import com.mybank.transfer.api.TransferRequest;
import com.mybank.transfer.api.TransferResponse;
import com.mybank.transfer.client.AccountProfileView;
import com.mybank.transfer.client.AccountsClient;
import com.mybank.transfer.client.NotificationsClient;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
        String sender = resolveSenderUsername();
        validateParticipants(sender, request.recipientUsername());

        // Validate recipient existence before balance changes.
        accountsClient.getByUsername(request.recipientUsername());

        AccountProfileView senderAfterWithdraw = accountsClient.withdraw(sender, request.amount());
        try {
            AccountProfileView recipientAfterDeposit = accountsClient.deposit(request.recipientUsername(), request.amount());
            return new TransferResponse(
                    "TRANSFER_SUCCESS",
                    sender,
                    request.recipientUsername(),
                    request.amount(),
                    senderAfterWithdraw.balance(),
                    recipientAfterDeposit.balance()
            );
            
        } catch (RuntimeException ex) {
            // Best-effort compensation for partial failure.
            accountsClient.deposit(sender, request.amount());
            throw ex;
        } finally {
            notificationsClient.send(
                    "TRANSFER_ATTEMPT",
                    "Transfer attempt from " + sender + " to " + request.recipientUsername()
            );
        }
    }

    private String resolveSenderUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            String preferred = jwt.getToken().getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            String sub = jwt.getToken().getSubject();
            if (sub != null && !sub.isBlank()) {
                return sub;
            }
        }
        return senderUsername;
    }

    private void validateParticipants(String sender, String recipientUsername) {
        if (Objects.equals(sender, recipientUsername)) {
            throw new TransferOperationException("sender and recipient must be different");
        }
    }
}
