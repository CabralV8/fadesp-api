package com.fadesp.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI fadespPaymentAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Pagamentos - FADESP")
                        .version("1.0.0")
                        .description("""
                                API REST para gerenciamento de pagamentos de débitos de pessoas físicas e jurídicas.
                                Inclui operações de criação, consulta, atualização de status e exclusão lógica.
                                """)
                        .contact(new Contact()
                                .name("Equipe Técnica FADESP")
                                .email("suporte@fadesp.com.br")
                                .url("https://www.fadesp.com.br"))
                        .license(new License()
                                .name("Licença MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
