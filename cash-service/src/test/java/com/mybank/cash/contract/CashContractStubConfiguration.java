package com.mybank.cash.contract;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration(proxyBeanMethods = false)
public class CashContractStubConfiguration {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
