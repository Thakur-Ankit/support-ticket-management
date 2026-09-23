package com.supporttickets.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank String content,
    @NotBlank String author
) {
}
