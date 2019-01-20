package me.sangmessi.soccer.accounts;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class AccountResource extends Resource<Account> {

    public AccountResource(Account account, Link... links) {
        super(account, links);
        add(linkTo(AccountController.class).slash(account.getEmail()).withSelfRel());
    }
}
