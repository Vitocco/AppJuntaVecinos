package com.zaror.finanza.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zaror.finanza.dto.PagoManualRequest;
import com.zaror.finanza.dto.PagoResponse;
import com.zaror.finanza.dto.WebpayIniciarRequest;
import com.zaror.finanza.dto.WebpayIniciarResponse;
import com.zaror.finanza.security.UsuarioToken;
import com.zaror.finanza.service.PagoService;
import com.zaror.finanza.service.WebpayService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final WebpayService webpayService;

    public PagoController(PagoService pagoService, WebpayService webpayService) {
        this.pagoService = pagoService;
        this.webpayService = webpayService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTIVA', 'TESORERO')")
    public List<PagoResponse> listar() {
        return pagoService.listar();
    }

    // multipart: "datos" (JSON) + "archivo" OBLIGATORIO (foto del comprobante)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TESORERO')")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse registrarManual(@Valid @RequestPart("datos") PagoManualRequest datos,
                                        @RequestPart("archivo") MultipartFile archivo,
                                        @AuthenticationPrincipal UsuarioToken usuario) {
        return pagoService.registrarManual(datos, archivo, usuario.id());
    }

    // ---- Webpay Plus (integración) ----

    @PostMapping("/webpay/iniciar")
    @PreAuthorize("hasRole('VECINO')")
    public WebpayIniciarResponse iniciarWebpay(@Valid @RequestBody WebpayIniciarRequest req,
                                               @AuthenticationPrincipal UsuarioToken usuario) {
        return webpayService.iniciar(req.reservaId(), usuario);
    }

    // Público: Transbank redirige aquí el navegador del vecino (GET o POST)
    @RequestMapping(value = "/webpay/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> retornoWebpay(@RequestParam(name = "token_ws", required = false) String tokenWs,
                                                @RequestParam(name = "TBK_TOKEN", required = false) String tbkToken,
                                                @RequestParam(name = "TBK_ORDEN_COMPRA", required = false) String tbkOrdenCompra) {
        boolean ok = webpayService.procesarRetorno(tokenWs, tbkToken, tbkOrdenCompra);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(paginaRetorno(ok));
    }

    // Página simple que devuelve al vecino a la app (deep link zaror://pago?estado=ok|error)
    private String paginaRetorno(boolean ok) {
        String deepLink = "zaror://pago?estado=" + (ok ? "ok" : "error");
        String titulo = ok ? "Pago aprobado" : "Pago rechazado";
        String mensaje = ok ? "Tu reserva quedó pagada." : "No se realizó ningún cargo. Puedes intentarlo nuevamente.";
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta http-equiv="refresh" content="2;url=%s">
                  <title>%s</title>
                  <style>
                    body { font-family: sans-serif; text-align: center; padding: 48px 16px; }
                    a { display: inline-block; margin-top: 24px; padding: 12px 24px; background: #1b5e20;
                        color: #fff; border-radius: 8px; text-decoration: none; }
                  </style>
                </head>
                <body>
                  <h1>%s</h1>
                  <p>%s</p>
                  <p>Vuelve a la app de la Junta Zaror.</p>
                  <a href="%s">Volver a la app</a>
                  <script>setTimeout(function () { window.location.href = "%s"; }, 1500);</script>
                </body>
                </html>
                """.formatted(deepLink, titulo, titulo, mensaje, deepLink, deepLink);
    }
}
