package org.example.scrd.base;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "custom")
public class CustomProperties {
    @Value("${custom.host.client}")
    private List<String> hostClient;
    @Value("${custom.jwt.secret}")
    private String jwtSecret;


}
