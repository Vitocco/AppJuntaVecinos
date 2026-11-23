package com.zaror.finanza.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.zaror.finanza.dto.ReservaDto;
import com.zaror.finanza.dto.WebpayIniciarResponse;
import com.zaror.finanza.model.EstadoPago;
import com.zaror.finanza.model.Pago;
import com.zaror.finanza.security.UsuarioToken;

import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;

// Webpay Plus de Transbank en AMBIENTE DE INTEGRACIÓN (IntegrationType.TEST): sin dinero real.
// Flujo: iniciar (create) -> el vecino paga en el formulario de Transbank -> retorno (commit)
@Service
public class WebpayService {

    private static final Logger log = LoggerFactory.getLogger(WebpayService.class);

    private final PagoService pagoService;
    private final WebpayPlus.Transaction transaccion;
    private final String returnUrl;

    public WebpayService(PagoService pagoService,
                         @Value("${webpay.commerce-code}") String commerceCode,
                         @Value("${webpay.api-key}") String apiKey,
                         @Value("${webpay.return-url}") String returnUrl) {
        this.pagoService = pagoService;
        this.transaccion = new WebpayPlus.Transaction(new WebpayOptions(commerceCode, apiKey, IntegrationType.TEST));
        this.returnUrl = returnUrl;
    }

    public WebpayIniciarResponse iniciar(Long reservaId, UsuarioToken usuario) {
        ReservaDto reserva = pagoService.validarReservaPagable(reservaId);
        // Solo el dueño de la reserva puede pagarla
        if (!reserva.usuarioId().equals(usuario.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el dueño puede pagar la reserva");
        }

        Pago pago = pagoService.crearPendienteWebpay(reserva);
        try {
            // buyOrder (máx. 26 caracteres) identifica nuestro pago; el monto es el precio de la sede
            WebpayPlusTransactionCreateResponse resp = transaccion.create(
                    "ZAROR-" + pago.getId(), "U" + usuario.id(), pago.getMonto(), returnUrl);
            pagoService.guardarToken(pago.getId(), resp.getToken());
            return new WebpayIniciarResponse(resp.getUrl(), resp.getToken());
        } catch (Exception e) {
            log.error("Error al crear transacción Webpay", e);
            pagoService.rechazar(pago.getId());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo iniciar el pago con Webpay");
        }
    }

    // Devuelve true si el pago quedó PAGADO
    public boolean procesarRetorno(String tokenWs, String tbkToken, String tbkOrdenCompra) {
        // Usuario anuló en el formulario (o error del formulario): llega TBK_TOKEN
        if (tbkToken != null) {
            Pago p = pagoService.buscarPorToken(tbkToken);
            if (p != null) {
                pagoService.rechazar(p.getId());
            }
            return false;
        }

        // Normal: llega token_ws -> confirmar con commit
        if (tokenWs != null) {
            Pago p = pagoService.buscarPorToken(tokenWs);
            if (p == null) {
                return false;
            }
            // Si recargan la página no se hace commit dos veces
            if (p.getEstado() != EstadoPago.PENDIENTE) {
                return p.getEstado() == EstadoPago.PAGADO;
            }
            try {
                WebpayPlusTransactionCommitResponse resp = transaccion.commit(tokenWs);
                if ("AUTHORIZED".equals(resp.getStatus()) && resp.getResponseCode() == 0) {
                    pagoService.confirmarWebpay(p.getId(), resp.getAuthorizationCode());
                    return true;
                }
            } catch (Exception e) {
                log.error("Error al confirmar transacción Webpay", e);
            }
            pagoService.rechazar(p.getId());
            return false;
        }

        // Tiempo agotado en el formulario: solo llega TBK_ORDEN_COMPRA ("ZAROR-<idPago>")
        if (tbkOrdenCompra != null && tbkOrdenCompra.startsWith("ZAROR-")) {
            try {
                Pago p = pagoService.buscarOpcional(Long.valueOf(tbkOrdenCompra.substring(6)));
                if (p != null) {
                    pagoService.rechazar(p.getId());
                }
            } catch (NumberFormatException ignored) {
                // orden de compra inválida: no hay nada que rechazar
            }
        }
        return false;
    }
}
