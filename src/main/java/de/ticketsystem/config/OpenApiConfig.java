package de.ticketsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IT Support Ticket System REST API")
                        .version("1.0.0")
                        .description("Objektorientierte Backend-Anwendung zur Erfassung und Verwaltung von IT-Support-Tickets. " +
                                     "Beinhaltet Statusvalidierung über State Machine, relationale SQL-Persistenz und Audit-Logging.")
                        .contact(new Contact()
                                .name("IT Support System Team")
                                .email("support-system@company.de"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
