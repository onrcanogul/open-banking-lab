package io.github.onrcanogul.openbanking.hhsbank.common.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountRef) {
        super(
                accountRef + " account not found!"
        );
    }
}
