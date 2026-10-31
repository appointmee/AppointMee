package com.gg.turnlook.auth;
import com.gg.turnlook.auth.dto.*;
import com.gg.turnlook.auth.password.PasswordResetService;
import com.gg.turnlook.features.usuario.UsuarioService;
import com.gg.turnlook.features.usuario.dto.UsuarioCrearDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;
    private final UsuarioService usuarios;
    private final PasswordResetService passwords;
    @PostMapping({"/auth/login", "/usuarios/inicio-sesion"})
    public LoginResponseDTO login(@Valid @RequestBody LoginDTO request) { return auth.login(request); }
    @PostMapping({"/auth/register", "/usuarios/crear"})
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody UsuarioCrearDTO request) {
        usuarios.crearUsuario(request);
        return ResponseEntity.status(201).body(new MessageResponse("Se creó al usuario"));
    }
    @PostMapping("/auth/refresh")
    public LoginResponseDTO refresh(@Valid @RequestBody RefreshTokenRequest request) { return auth.renovar(request.refreshToken()); }
    @PostMapping("/auth/logout")
    public MessageResponse logout(@AuthenticationPrincipal String email) {
        auth.logout(email); return new MessageResponse("Sesión cerrada");
    }
    @PostMapping("/auth/forgot-password")
    public MessageResponse forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwords.solicitar(request.email());
        } catch (org.springframework.web.client.RestClientException e) {
            // Do not expose whether a recipient exists when the email provider fails.
            org.slf4j.LoggerFactory.getLogger(AuthController.class).warn("No se pudo enviar el correo de recuperación");
        }
        return new MessageResponse("Si el correo está registrado, recibirás un enlace de recuperación.");
    }
    @PostMapping("/auth/reset-password")
    public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest request) {
        passwords.restablecer(request.token(), request.newPassword());
        return new MessageResponse("Contraseña restablecida. Iniciá sesión nuevamente.");
    }
    @PostMapping("/auth/change-password")
    public MessageResponse change(@AuthenticationPrincipal String email, @Valid @RequestBody ChangePasswordRequest request) {
        auth.cambiarPassword(email, request);
        return new MessageResponse("Contraseña modificada. Iniciá sesión nuevamente.");
    }
}
