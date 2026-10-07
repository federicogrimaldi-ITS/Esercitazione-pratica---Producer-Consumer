package com.savoia.productclient;

import com.savoia.productclient.config.ApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductClientConfigurationTest {

    @Autowired
    ApiProperties apiProperties;

    @Test
    void producerApiIsOnPort8081() {
        assertThat(apiProperties.baseUrl()).isEqualTo("http://localhost:8081/api");
    }

    @Test
    void categoriesMirrorTheProducerCategoryEnum() {
        assertThat(apiProperties.categories()).containsExactly(
                "Accessori", "Audio", "Informatica", "Mobile",
                "Networking", "Storage", "Ufficio", "Wearable");
    }
}
