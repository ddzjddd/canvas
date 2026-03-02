package com.canvas.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class WorkflowDtos {
    public record WorkflowCreate(@NotBlank String name, @NotNull JsonNode graphJson) {}
    public record RunCreate(Map<String, Object> inputs) {}
}
