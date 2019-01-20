package me.sangmessi.soccer.accounts;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class AccountDto {
    @NotEmpty
    private Integer id;
    @NotEmpty
    private String email;
    private String password;
    @NotEmpty
    private String name;
    private Set<AccountRole> roles;
}
