package me.sangmessi.soccer.accounts;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class AccountValidator {

    private final AccountService accountService;

    public AccountValidator(AccountService accountService) {
        this.accountService = accountService;
    }

    public void validate(Account account, Errors errors) {
        if(accountService.existsUser(account)) {
            errors.rejectValue("email", "exists User","This account has already been registered ("+account.getEmail()+")");
        }
    }


}
