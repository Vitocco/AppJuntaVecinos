package com.zaror.finanza.dto;

// La app abre en el navegador: url + "?token_ws=" + token
public record WebpayIniciarResponse(String url, String token) {
}
