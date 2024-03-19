package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessorImplTest {

    @Mock
    AccountService accountService;

    @InjectMocks
    PaymentProcessorImpl paymentProcessor;

    @Test
    public void testTransfer() {
        // Test data
        int sourceType = 0;
        int destinationType = 0;

        Agreement sourceAgreement = new Agreement();
        Long sourceAgreementId = 1L;
        sourceAgreement.setId(sourceAgreementId);

        Agreement destinationAgreement = new Agreement();
        Long destinationAgreementId = 2L;
        destinationAgreement.setId(destinationAgreementId);

        Account sourceAccount = new Account();
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(sourceType);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(destinationType);

        // Mocks
        when(accountService.getAccounts(argThat(argument -> argument != null && argument.getId().equals(sourceAgreementId))))
            .thenReturn(List.of(sourceAccount));

        when(accountService.getAccounts(argThat(argument -> argument != null && argument.getId().equals(destinationAgreementId))))
            .thenReturn(List.of(destinationAccount));

        // Invoke
        paymentProcessor.makeTransfer(sourceAgreement, destinationAgreement,
            sourceType, destinationType, BigDecimal.ONE);
    }

    @Test
    public void makeTransferWithCommission() {
        // Test data
        int sourceType = 0;
        int destinationType = 0;
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal commissionPercent = BigDecimal.TEN;
        BigDecimal commission = amount.multiply(commissionPercent);

        Agreement sourceAgreement = new Agreement();
        Long sourceAgreementId = 1L;
        sourceAgreement.setId(sourceAgreementId);

        Agreement destinationAgreement = new Agreement();
        Long destinationAgreementId = 2L;
        destinationAgreement.setId(destinationAgreementId);

        Account sourceAccount = new Account();
        sourceAccount.setAmount(BigDecimal.TEN);
        sourceAccount.setType(sourceType);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(BigDecimal.ZERO);
        destinationAccount.setType(destinationType);

        // Mocks
        when(accountService.getAccounts(argThat(argument -> argument != null && argument.getId().equals(sourceAgreementId))))
            .thenReturn(List.of(sourceAccount));

        when(accountService.getAccounts(argThat(argument -> argument != null && argument.getId().equals(destinationAgreementId))))
            .thenReturn(List.of(destinationAccount));

        // Invoke
        paymentProcessor.makeTransferWithCommission(sourceAgreement, destinationAgreement, sourceType, destinationType, amount, commissionPercent);

        // Verifications
        verify(accountService, times(2)).getAccounts(any());
        verify(accountService).charge(sourceAccount.getId(), commission);
        verify(accountService).makeTransfer(sourceAccount.getId(), destinationAccount.getId(), amount);
    }

}
