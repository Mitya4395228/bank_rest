package com.example.bankcards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.string.encryptor")
public record StringEncryptorProperties(String algorithm, String secretKey) {

}
