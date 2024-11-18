package org.example.scrd.base;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "custom")
public class CustomProperties {
    private List<String> hostClient;
    private String jwtSecret;


}
