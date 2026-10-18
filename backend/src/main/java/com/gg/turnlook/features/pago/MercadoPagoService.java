package com.gg.turnlook.features.pago;

import com.gg.turnlook.common.exception.ConflictException;
import com.gg.turnlook.common.exception.ForbiddenException;
import com.gg.turnlook.features.servicio.Servicio;
import com.gg.turnlook.features.turno.EstadoTurno;
import com.gg.turnlook.features.turno.Turno;
import com.gg.turnlook.features.turno.TurnoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;






@Service
public class MercadoPagoService {



    private final TurnoService turnoService;


    // mp test
    @Value("${mercadopago.access-token}")
    private String tokenMp;



    public MercadoPagoService(TurnoService turnoService) {
        this.turnoService = turnoService;
    }



    /// METODOS


    public String pagar(Integer turnoId, String clienteEmail)
            throws MPException, MPApiException {

        MercadoPagoConfig.setAccessToken(tokenMp);

        Turno turno = turnoService.listarTurnoPorId(turnoId);

        if(!turno.getCliente().getEmail().equals(clienteEmail)){
            throw new ForbiddenException("No tenes permisos");
        }

        if(turno.getEstado() != EstadoTurno.PENDIENTE){
            throw new ConflictException("Solo se pueden pagar turnos pendientes");
        }

        Servicio servicio = turno.getServicio();

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(servicio.getNombre())
                .description(servicio.getDescripcion())
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(servicio.getPrecio())
                .build();

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(item);

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .externalReference(turno.getId().toString())
                .notificationUrl("https://appointmee-vcs2.onrender.com/pagos/webhook")
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        return preference.getInitPoint();
    }


    public String procesarWebhook(Long pagoId) throws MPException, MPApiException {

        MercadoPagoConfig.setAccessToken(tokenMp);

        PaymentClient client = new PaymentClient();

        Payment payment = client.get(pagoId);

        Integer turnoId = Integer.valueOf(payment.getExternalReference());

        if (!"approved".equals(payment.getStatus())) {

            turnoService.setearPagoRechazado(turnoId);
            return "Error al realizar el pago";
        }

        return "Pago exitoso\nTurno confirmado";
    }


}


