package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @Test
    void testTransfer() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        assertEquals(new BigDecimal(90), sourceAccount.getAmount());
        assertEquals(new BigDecimal(20), destinationAccount.getAmount());
    }

    @Test
    void accountNotFoundForTransfer() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));
            }
        });
        assertEquals("No source account", result.getLocalizedMessage());
    }

    @Test
    void testTransferWithVerify() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        ArgumentMatcher<Account> sourceMatcher =
                argument -> argument.getId().equals(1L) && argument.getAmount().equals(new BigDecimal(90));

        ArgumentMatcher<Account> destinationMatcher =
                argument -> argument.getId().equals(2L) && argument.getAmount().equals(new BigDecimal(20));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        verify(accountDao).save(argThat(sourceMatcher));
        verify(accountDao).save(argThat(destinationMatcher));
    }

    @Test
    void addingAccount() {
        // Test data
        Agreement agreement = new Agreement();
        agreement.setId(1L);
        String accountNumber = "123";
        Integer type = 2;
        BigDecimal amount = new BigDecimal(100);

        // Invoke
        accountServiceImpl.addAccount(agreement, accountNumber, type, amount);

        // Captors
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // Verifications
        verify(accountDao).save(captor.capture());

        assertThat(captor.getValue())
            .matches(it -> it.getAgreementId().equals(agreement.getId()))
            .matches(it -> it.getNumber().equals(accountNumber))
            .matches(it -> it.getType().equals(type))
            .matches(it -> it.getAmount().equals(amount));
    }

    @Test
    void getAccounts() {
        // Test data
        Account account1 = new Account();
        Account account2 = new Account();
        List<Account> accounts = List.of(account1, account2);

        // Mocks
        when(accountDao.findAll()).thenReturn(accounts);

        // Verifications
        List<Account> receivedAccounts = accountServiceImpl.getAccounts();
        assertThat(receivedAccounts).isEqualTo(accounts);
    }

    @Test
    void getAccountsForAgreement() {
        // Test data
        Agreement agreement = new Agreement();
        agreement.setId(1L);
        Account account1 = new Account();
        Account account2 = new Account();
        List<Account> accounts = List.of(account1, account2);

        // Mocks
        when(accountDao.findByAgreementId(agreement.getId())).thenReturn(accounts);

        // Verifications
        List<Account> receivedAccounts = accountServiceImpl.getAccounts(agreement);
        assertThat(receivedAccounts).isEqualTo(accounts);
    }

    @Test
    void charging() {
        // Test data
        Account account = new Account();
        BigDecimal amount = new BigDecimal(100);
        account.setAmount(amount);
        account.setId(1L);
        BigDecimal chargeAmount = new BigDecimal(50);

        // Mocks
        when(accountDao.findById(account.getId())).thenReturn(Optional.of(account));

        // Invoke
        assertThat(accountServiceImpl.charge(account.getId(),chargeAmount)).isTrue();

        // Captors
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // Verifications
        verify(accountDao).save(captor.capture());

        assertThat(captor.getValue().getAmount()).isEqualTo(amount.subtract(chargeAmount));
    }
}
