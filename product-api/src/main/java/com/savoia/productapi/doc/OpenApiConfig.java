package com.savoia.productapi.doc;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@SecurityScheme(
        name = "bearerAuth",
        description = "JWT user",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI defineOpenApi() {

        Server serverDev = new Server();
        serverDev.setUrl("http://localhost:8080");
        serverDev.setDescription("Development");

        SecurityRequirement security = new SecurityRequirement();
        security.addList("bearerAuth");

        Info info = new Info()
                .title("Management System API")
                .version("0.1")
                .description("REST API documentation.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(serverDev))
                .security(List.of(security));
    }
}
