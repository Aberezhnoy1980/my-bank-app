package com.mybank.cash.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.cash.client.AccountProfileView;
import com.mybank.cash.client.AccountsClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {AccountsClient.class, CashContractStubConfiguration.class, JacksonAutoConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "com.mybank:accounts-service:0.1.0-SNAPSHOT:stubs:6565"
)
@TestPropertySource(properties = "app.accounts.base-url=http://127.0.0.1:6565")
class CashToAccountsContractIT {

    @Autowired
    private AccountsClient accountsClient;

    @Test
    void depositMatchesAccountsStub() {
        AccountProfileView view = accountsClient.deposit(new BigDecimal("100"));
        assertThat(view.username()).isEqualTo("demo.user");
        assertThat(view.fullName()).isEqualTo("Demo User");
        assertThat(view.balance()).isEqualByComparingTo("10100.00");
    }
}
