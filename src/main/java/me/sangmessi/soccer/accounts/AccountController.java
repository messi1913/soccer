package me.sangmessi.soccer.accounts;

import me.sangmessi.soccer.commons.ErrorResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "api/accounts", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class AccountController {

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final AccountValidator accountValidator;

    private final AccountRepository accountRepository;

    public AccountController(AccountService accountService, ModelMapper modelMapper, AccountValidator accountValidator, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.modelMapper = modelMapper;
        this.accountValidator = accountValidator;
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public ResponseEntity createAccount(@RequestBody @Valid @CurrentUser Account account,
                                        Errors errors) {
        if(errors.hasErrors())
            return badRequest(errors);

        accountValidator.validate(account, errors);

        if(errors.hasErrors())
            return badRequest(errors);

        Account newAccount = this.accountService.saveAccount(account);
        ControllerLinkBuilder linkBuilder = linkTo(AccountController.class).slash(newAccount.getEmail());
        URI createUri = linkBuilder.toUri();
        AccountResource accountResource = new AccountResource(account);
        accountResource.add(linkTo(AccountController.class).withRel("query-accounts"));
        accountResource.add(new Link("/docs/index.html#resources-accounts-create").withRel("profile"));
        return ResponseEntity.created(createUri).body(accountResource);
    }

    @GetMapping
    public ResponseEntity queryAccounts(Pageable pageable,
                                        PagedResourcesAssembler<Account> assembler,
                                        @CurrentUser Account currentUser){
        Page<Account> accounts = this.accountRepository.findAll(pageable);
        var accountResource = assembler.toResource(accounts, e -> new AccountResource(e));
        accountResource.add(new Link("/docs/index.html#resources-accounts-list").withRel("profile"));
        if(currentUser != null) {
            accountResource.add(linkTo(AccountController.class).withRel("create-account"));
        }
        return ResponseEntity.ok(accountResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity getAccount(@PathVariable Integer id, @CurrentUser Account currentUser){
        Optional<Account> accountOptional = this.accountRepository.findById(id);
        if(accountOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Account account = accountOptional.get();
        AccountResource accountResource = new AccountResource(account);
        accountResource.add(new Link("/docs/index.html#resources-accounts-get").withRel("profile"));
        if(currentUser.getRoles().contains(AccountRole.ADMIN)){
            accountResource.add(linkTo(AccountController.class).slash(account.getId()).withRel("update-account"));
        }

        return ResponseEntity.ok(accountResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateAccount(@PathVariable Integer id,
                                        @RequestBody @Valid Account account,
                                        Errors errors,
                                        @CurrentUser Account currentUser){

        Optional<Account> accountOptional = this.accountRepository.findById(id);
        if(accountOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        if(errors.hasErrors()){
            return badRequest(errors);
        }

        //TODO : 현재 사용자의 권한 체크 (본인계정 이거나 관리자만 수정가능)
        this.accountService.saveAccount(account);

        AccountResource accountResource = new AccountResource(account);
        accountResource.add(new Link("/docs/index.html#resources-accounts-update").withRel("profile"));
        if(currentUser.getRoles().contains(AccountRole.ADMIN)){
            accountResource.add(linkTo(AccountController.class).slash(account.getId()).withRel("get-account"));
        }

        return ResponseEntity.ok(accountResource);
    }


    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorResource(errors));
    }


}
