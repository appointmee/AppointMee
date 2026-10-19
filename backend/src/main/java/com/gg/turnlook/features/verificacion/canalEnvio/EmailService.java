package com.gg.turnlook.features.verificacion.canalEnvio;

import com.gg.turnlook.features.verificacion.CanalEnvioService;
import com.gg.turnlook.features.verificacion.enums.CanalVerificacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService implements CanalEnvioService {

    private final RestClient cliente;
    private final String remitente;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from}") String remitente) {

        this.remitente = remitente;

        this.cliente = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public CanalVerificacion getCanal() {
        return CanalVerificacion.EMAIL;
    }

    @Override
    public void enviarCodigo(String destinatario, String codigo) {
        cliente.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", remitente,
                        "to", List.of(destinatario),
                        "subject", "RECONTRAPUTO al que le llega",
                        "text", "Tu código de verificación es: " + codigo
                                + "\nVence en 5 minutos."
                                + "\nSi no lo solicitaste, ignorá este correo."
                ))
                .retrieve()
                .toBodilessEntity();
    }
}