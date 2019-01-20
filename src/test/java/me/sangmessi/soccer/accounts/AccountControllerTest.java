package me.sangmessi.soccer.accounts;

import me.sangmessi.soccer.common.BaseControllerTest;
import me.sangmessi.soccer.commons.TestDescription;
import me.sangmessi.soccer.configs.AppProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;


    @Before
    public void setup(){
        this.accountRepository.deleteAll();
    }

    @Test
    @TestDescription("회원가입을 처리하는 테스트 ")
    public void createUserAccount() throws Exception {
        String email = "messi1913@gmail.com";
        String password = "rlatkdap1";
        Account account = Account.builder()
                .email(email)
                .password(password)
                .name("상메시")
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        mockMvc.perform(post("/api/accounts")
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(account)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("create-account",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-accounts").description("link to query events"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        relaxedRequestFields(
                                fieldWithPath("email").description("Email of User"),
                                fieldWithPath("name").description("name of User"),
                                fieldWithPath("roles").description("roles of User")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("Identification of User"),
                                fieldWithPath("email").description("Email of User"),
                                fieldWithPath("name").description("name of User"),
                                fieldWithPath("roles").description("roles of User"),
                                fieldWithPath("_links.self.href").description("Link to self"),
                                fieldWithPath("_links.query-accounts.href").description("Link to query events list"),
                                fieldWithPath("_links.profile.href").description("Link to update existing event")

                        )
                ))

        ;
    }

    @Test
    @TestDescription("사용자 계정 정보와 함께 30개의 사용자를 10개씩 두번 조회하기")
    public void getUsers()  throws Exception {
        IntStream.range(0, 30).forEach(this::generateAccounts);

        this.mockMvc.perform(get("/api/accounts")
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .accept(MediaTypes.HAL_JSON)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.accountList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-account").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andDo(document("get-accounts",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("create-account").description("link to create user"),
                                linkWithRel("first").description("First page to list of user"),
                                linkWithRel("prev").description("Previous page to list of user"),
                                linkWithRel("next").description("Next page to list of user"),
                                linkWithRel("last").description("Last page to list of user"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestParameters(
                                parameterWithName("page").description("The number of start page (0~last)"),
                                parameterWithName("size").description("The size of elements"),
                                parameterWithName("sort").description("soring")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("page.size").description("Size of page"),
                                fieldWithPath("page.totalElements").description("The number of total elements"),
                                fieldWithPath("page.totalPages").description("The number of total pages"),
                                fieldWithPath("page.number").description("The current page number"),
                                fieldWithPath("_embedded.accountList").description("list of User"),
                                fieldWithPath("_links.self.href").description("Link to self"),
                                fieldWithPath("_links.profile.href").description("Link to list existing users")

                        )
                        ))
                ;
    }

    @Test
    @TestDescription("ID 를 통해서 사용자 계정을 조회한다. ")
    public void getUserById() throws Exception {
        Account account = this.generateAccounts(100);

        this.mockMvc.perform(get("/api/accounts/{id}", account.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("email").exists())
                .andExpect(jsonPath("roles").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(print())
                .andDo(document("get-account",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("update-account").description("link to update account"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("Identification of User"),
                                fieldWithPath("email").description("Email of User"),
                                fieldWithPath("name").description("name of User"),
                                fieldWithPath("roles").description("roles of User"),
                                fieldWithPath("_links.self.href").description("Link to self"),
                                fieldWithPath("_links.update-account.href").description("Link to update user"),
                                fieldWithPath("_links.profile.href").description("Link to get account")
                        )

                ))

        ;
    }

    @Test
    @TestDescription("기존 사용자를 정상적으로 수정하기 ")
    public void updateAccount() throws Exception {

        Account account = this.generateAccounts(100);
        String username = "Update User";
        account.setName(username);

        this.mockMvc.perform(put("/api/accounts/{id}", account.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(account))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("name").value(username))
            .andExpect(jsonPath("_links.self").exists())
            .andDo(document("update-account",
                    links(
                            linkWithRel("self").description("link to self"),
                            linkWithRel("get-account").description("link to update account"),
                            linkWithRel("profile").description("link to profile")
                    ),
                    requestHeaders(
                            headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                    ),
                    relaxedRequestFields(
                            fieldWithPath("email").description("Email of User"),
                            fieldWithPath("name").description("name of User"),
                            fieldWithPath("roles").description("roles of User")
                    ),
                    responseHeaders(
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                    ),
                    relaxedResponseFields(
                            fieldWithPath("id").description("Identification of User"),
                            fieldWithPath("email").description("Email of User"),
                            fieldWithPath("name").description("name of User"),
                            fieldWithPath("roles").description("roles of User"),
                            fieldWithPath("_links.self.href").description("Link to self"),
                            fieldWithPath("_links.get-account.href").description("Link to get user"),
                            fieldWithPath("_links.profile.href").description("Link to get account")
                    )
            ))

        ;


    }

    private Account generateAccounts(int index) {
        Account account = Account.builder()
                    .name("TEST"+index)
                    .password("password")
                    .email("TEST"+index+"@gmail.com")
                    .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                    .build();

        return this.accountRepository.save(account);
    }

    private String getAccessToken() throws Exception {
        String clientId = appProperties.getClientId();
        String password = appProperties.getClientSecret();

        String username = appProperties.getAdminUsername();
        String userPassword = appProperties.getAdminPassword();
        Account admin = Account.builder()
                .email(username)
                .password(userPassword)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(admin);

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(clientId, password))
                .param("username", username)
                .param("password", userPassword)
                .param("grant_type", "password")
        );
        String content = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser jsonParser = new Jackson2JsonParser();
        return jsonParser.parseMap(content).get("access_token").toString();
    }

    private String getBearerToken() throws Exception{
        return "Bearer "+getAccessToken();
    }
}
