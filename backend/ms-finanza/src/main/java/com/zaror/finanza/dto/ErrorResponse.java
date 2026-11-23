package com.zaror.finanza.dto;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String error, String mensaje, LocalDateTime timestamp) {
}
