package com.gg.turnlook.features.verificacion;

import com.gg.turnlook.auth.BloqueoUsuario;
import com.gg.turnlook.common.exception.BadRequestException;
import com.gg.turnlook.features.usuario.Usuario;
import com.gg.turnlook.features.usuario.UsuarioRepository;
import com.gg.turnlook.features.verificacion.canalEnvio.EmailService;
import com.gg.turnlook.features.verificacion.enums.CanalVerificacion;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificacionEmailService {

    private final UsuarioRepository usuarios;
    private final VerificacionRepository verificaciones;
    private final BloqueoUsuario bloqueo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();


    @Transactional
    public void solicitar(String email) {
        String destinatario = normalizar(email);
        var encontrado = usuarios.findByEmailAndActivoTrue(destinatario);
        if (encontrado.isEmpty()) return;

        Usuario usuario = bloqueo.bloquear(encontrado.get().getId());
        if (!usuario.isActivo() || usuario.isEmailVerificado()
                || !destinatario.equals(usuario.getEmail())) return;

        Instant ahora = Instant.now();
        var ultima = verificaciones.findFirstByUsuarioIdAndCanalOrderByFechaCreacionDescIdDesc(
                usuario.getId(), CanalVerificacion.EMAIL);

        // Espera silenciosa de 60 segundos sin revelar si el destinatario existe.
        if (ultima.isPresent() && ahora.isBefore(ultima.get().getFechaCreacion().plusSeconds(60))) return;

        verificaciones.invalidarPorUsuario(usuario.getId(), ahora);
        String codigo;
        do {
            codigo = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        } while (ultima.isPresent() && encoder.matches(codigo, ultima.get().getCodigoHash()));
        verificaciones.save(Verificacion.builder()
                .usuario(usuario)
                .canal(CanalVerificacion.EMAIL)
                .destinatario(destinatario)
                .codigoHash(encoder.encode(codigo))
                .fechaCreacion(ahora)
                .fechaVencimiento(ahora.plusSeconds(300))
                .intentosFallidos(0)
                .build());
        emailService.enviarCodigo(destinatario, codigo);
    }


    // Persistir los intentos incorrectos aunque se responda 400.
    @Transactional(noRollbackFor = BadRequestException.class)
    public void confirmar(String email, String codigo) {
        String destinatario = normalizar(email);
        Usuario encontrado = usuarios.findByEmailAndActivoTrue(destinatario)
                .orElseThrow(VerificacionEmailService::codigoInvalido);
        Usuario usuario = bloqueo.bloquear(encontrado.getId());
        if (!usuario.isActivo() || usuario.isEmailVerificado()
                || !destinatario.equals(usuario.getEmail())) throw codigoInvalido();

        Verificacion verificacion = verificaciones
                .findFirstByUsuarioIdAndCanalOrderByFechaCreacionDescIdDesc(usuario.getId(), CanalVerificacion.EMAIL)
                .orElseThrow(VerificacionEmailService::codigoInvalido);
        Instant ahora = Instant.now();
        if (!destinatario.equals(verificacion.getDestinatario())
                || verificacion.getFechaUso() != null
                || !ahora.isBefore(verificacion.getFechaVencimiento())
                || verificacion.getIntentosFallidos() >= 5) throw codigoInvalido();

        if (!encoder.matches(codigo, verificacion.getCodigoHash())) {
            verificacion.setIntentosFallidos(verificacion.getIntentosFallidos() + 1);
            throw codigoInvalido();
        }
        usuario.setEmailVerificado(true);
        verificacion.setFechaUso(ahora);
        verificaciones.invalidarPorUsuario(usuario.getId(), ahora);
    }


    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static BadRequestException codigoInvalido() {
        return new BadRequestException("Código inválido, vencido o utilizado");
    }
}
