package com.github.fileshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Limite máximo de armazenamento por usuário (em bytes)
     */
    private long defaultMaxPerUser;
    
    private int defaultMaxVideoRetentionDays;

    /**
     * Limite máximo de armazenamento total do sistema (em bytes)
     */
    private long maxTotal;
}