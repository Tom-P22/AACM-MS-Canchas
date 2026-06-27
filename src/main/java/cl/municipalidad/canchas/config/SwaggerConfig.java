package cl.municipalidad.canchas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerToken";
        
        return new OpenAPI()
                
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Introduce el token JWT obtenido desde ms-auth para interactuar con los endpoints protegidos.")))

                .info(new Info()
                        .title("Municipalidad - API de Gestión de Canchas")
                        .version("1.0.0")
                        .description("Microservicio encargado de la administración de complejos deportivos, recintos municipales y auditoría de canchas.")
                        .contact(new Contact()
                                .name("Departamento de TI Municipalidad")
                                .email("soporte.ti@municipalidad.cl"))
                        .license(new License()
                                .name("Licencia Propietaria Municipal")
                                .url("https://municipalidad.cl")));
    }
}