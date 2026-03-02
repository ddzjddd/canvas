package com.canvas.dto;

import jakarta.validation.constraints.NotBlank;

public class ResourceDtos {
    public record MySqlConnCreate(
            @NotBlank String name,
            @NotBlank String host,
            Integer port,
            @NotBlank String database,
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record VpsHostCreate(
            @NotBlank String name,
            @NotBlank String host,
            Integer port,
            @NotBlank String username,
            @NotBlank String privateKey,
            String knownHostsPolicy
    ) {}
}
