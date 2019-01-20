package me.sangmessi.soccer.index;

import me.sangmessi.soccer.accounts.AccountController;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public ResourceSupport accountIndex(){
        var index = new ResourceSupport();
        index.add(linkTo(AccountController.class).withRel("accounts"));
        return index;
    }
}
