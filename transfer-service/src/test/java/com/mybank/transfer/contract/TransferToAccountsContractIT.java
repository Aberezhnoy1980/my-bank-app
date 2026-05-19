package com.mybank.transfer.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.mybank.transfer.client.AccountProfileView;
import com.mybank.transfer.client.AccountsClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {AccountsClient.class, TransferContractStubConfiguration.class, JacksonAutoConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "com.mybank:accounts-service:0.1.0-SNAPSHOT:stubs:6566"
)
@TestPropertySource(properties = "app.accounts.base-url=http://127.0.0.1:6566")
class TransferToAccountsContractIT {

    @Autowired
    private AccountsClient accountsClient;

    @Test
    void getByUsernameMatchesAccountsStub() {
        AccountProfileView view = accountsClient.getByUsername("alice.user");
        assertThat(view.username()).isEqualTo("alice.user");
        assertThat(view.fullName()).isEqualTo("Alice User");
        assertThat(view.balance()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }
}
