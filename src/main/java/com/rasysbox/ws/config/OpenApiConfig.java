package com.rasysbox.ws.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private final OpenApiProperties openApiProperties;

    @Autowired
    public OpenApiConfig(OpenApiProperties openApiProperties) {
        this.openApiProperties = openApiProperties;
    }

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://" + this.openApiProperties.getServerHost() + ":" + this.openApiProperties.getServerPort());
        devServer.setDescription(this.openApiProperties.getProjectShortDescription());

        Contact contact = new Contact();
        contact.setEmail(this.openApiProperties.getDeveloperMail());
        contact.setName(this.openApiProperties.getDeveloperName());
        contact.setUrl(this.openApiProperties.getOrganizationUrl());

        License mitLicense = new License()
                .name("MIT License")
                .url(this.openApiProperties.getProjectLicenceLink());

        Info info = new Info()
                .title(this.openApiProperties.getProjectName())
                .version("1.0")
                .contact(contact)
                .description(this.openApiProperties.getProjectShortDescription())
                .termsOfService(this.openApiProperties.getProjectLicenceMsg())
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
