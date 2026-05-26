package com.mybank.transfer.service;

import com.mybank.transfer.api.TransferRequest;
import com.mybank.transfer.api.TransferResponse;
import com.mybank.transfer.client.AccountProfileView;
import com.mybank.transfer.client.AccountsClient;
import com.mybank.transfer.kafka.NotificationEventPublisher;
import com.mybank.transfer.persistence.TransferRecordEntity;
import com.mybank.transfer.persistence.TransferRecordRepository;
import com.mybank.security.support.JwtUsernameResolver;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationEventPublisher notificationEventPublisher;
    private final TransferRecordRepository transferRecordRepository;
    private final JwtUsernameResolver jwtUsernameResolver;
    private final String senderUsername;

    public TransferService(
            AccountsClient accountsClient,
            NotificationEventPublisher notificationEventPublisher,
            TransferRecordRepository transferRecordRepository,
            JwtUsernameResolver jwtUsernameResolver,
            @Value("${app.transfer.sender-username}") String senderUsername
    ) {
        this.accountsClient = accountsClient;
        this.notificationEventPublisher = notificationEventPublisher;
        this.transferRecordRepository = transferRecordRepository;
        this.jwtUsernameResolver = jwtUsernameResolver;
        this.senderUsername = senderUsername;
    }

    public TransferResponse transfer(TransferRequest request) {
        String sender = jwtUsernameResolver.resolve(senderUsername);
        validateParticipants(sender, request.recipientUsername());

        // Validate recipient existence before balance changes.
        accountsClient.getByUsername(request.recipientUsername());

        AccountProfileView senderAfterWithdraw = accountsClient.withdraw(sender, request.amount());
        try {
            AccountProfileView recipientAfterDeposit = accountsClient.deposit(request.recipientUsername(), request.amount());
            transferRecordRepository.save(new TransferRecordEntity(sender, request.recipientUsername(), request.amount()));
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
            notificationEventPublisher.send(
                    "TRANSFER_ATTEMPT",
                    "Transfer attempt from " + sender + " to " + request.recipientUsername()
            );
        }
    }

    private void validateParticipants(String sender, String recipientUsername) {
        if (Objects.equals(sender, recipientUsername)) {
            throw new TransferOperationException("sender and recipient must be different");
        }
    }
}
