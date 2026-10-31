package com.gg.turnlook.auth;

import com.gg.turnlook.auth.dto.*;
import com.gg.turnlook.auth.jwt.JwtService;
import com.gg.turnlook.auth.refresh.*;
import com.gg.turnlook.auth.password.*;
import com.gg.turnlook.features.usuario.*;
import com.gg.turnlook.features.usuario.rol.*;
import com.gg.turnlook.features.verificacion.canalEnvio.EmailService;
import com.gg.turnlook.common.exception.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:auth-integration;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false",
    "jwt.secret=local-test-secret-only-32-characters-minimum",
    "resend.api-key=test", "resend.from=test@example.com",
    "whatsapp.gateway.url=http://localhost:9999", "whatsapp.gateway.api-key=test",
    "mercadopago.access-token=test", "cloudinary.cloud-name=test",
    "cloudinary.api-key=test", "cloudinary.api-secret=test"
})
class AuthIntegrationTest {
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @Autowired AuthService auth;
    @Autowired PasswordResetService passwords;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordResetTokenRepository resetTokens;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean EmailService email;
    private Usuario usuario;
    private static final String PASSWORD = "Password123!";
    @BeforeEach void preparar() {
        usuario = usuarios.saveAndFlush(new Usuario("Ana", "Perez", UUID.randomUUID() + "@example.com", encoder.encode(PASSWORD)));
    }
    @AfterEach void limpiar() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            refreshTokens.revocarPorUsuario(usuario.getId());
            resetTokens.revocarPorUsuario(usuario.getId());
            usuarios.deleteById(usuario.getId());
        });
    }
    private LoginResponseDTO login() { return auth.login(new LoginDTO(usuario.getEmail(), PASSWORD)); }
    @Test void rotaYRechazaReutilizacionYNoGuardaElTokenOriginal() {
        var primero = login();
        assertNotEquals(primero.getToken(), primero.getRefreshToken());
        assertEquals(TokenSeguro.hash(primero.getRefreshToken()), refreshTokens.findByUsuarioId(usuario.getId()).orElseThrow().getTokenHash());
        var segundo = auth.renovar(primero.getRefreshToken());
        assertNotEquals(primero.getRefreshToken(), segundo.getRefreshToken());
        assertThrows(UnauthorizedException.class, () -> auth.renovar(primero.getRefreshToken()));
        assertThrows(UnauthorizedException.class, () -> auth.renovar(segundo.getToken()));
        assertThrows(io.jsonwebtoken.JwtException.class, () -> jwt.validarAccessToken(segundo.getRefreshToken()));
    }
    @Test void unaSolaRenovacionConcurrenteTieneExito() throws Exception {
        var sesion = login();
        var inicio = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> tarea = () -> { inicio.await(); try { auth.renovar(sesion.getRefreshToken()); return true; } catch (UnauthorizedException ex) { return false; } };
            var a = pool.submit(tarea); var b = pool.submit(tarea); inicio.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
    }
    @Test void logoutYSegundoLoginRevocanRefresh() {
        var anterior = login(); var actual = login();
        assertThrows(UnauthorizedException.class, () -> auth.renovar(anterior.getRefreshToken()));
        auth.logout(usuario.getEmail());
        assertThrows(UnauthorizedException.class, () -> auth.renovar(actual.getRefreshToken()));
    }
    @Test void rechazaRefreshVencidoYCuentaInactiva() {
        var sesion = login();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var token = refreshTokens.findByUsuarioId(usuario.getId()).orElseThrow();
            token.setFechaVencimiento(Instant.now().minusSeconds(1));
        });
        assertThrows(UnauthorizedException.class, () -> auth.renovar(sesion.getRefreshToken()));
        var nueva = login();
        usuario.setActivo(false); usuarios.saveAndFlush(usuario);
        assertThrows(UnauthorizedException.class, () -> auth.renovar(nueva.getRefreshToken()));
    }
    @Test void recuperacionDeUnSoloUsoRevocaSesionesYActualizaHash() {
        var sesion = login();
        var enlace = new AtomicReference<String>();
        doAnswer(invocation -> { enlace.set(invocation.getArgument(1)); return null; }).when(email).enviarRecuperacion(eq(usuario.getEmail()), anyString(), anyLong());
        passwords.solicitar(usuario.getEmail());
        String token = enlace.get().split("token=")[1];
        assertTrue(resetTokens.findByTokenHash(TokenSeguro.hash(token)).isPresent());
        passwords.restablecer(token, "NuevaPassword123!");
        assertTrue(encoder.matches("NuevaPassword123!", usuarios.findById(usuario.getId()).orElseThrow().getPassword()));
        assertEquals(1, usuarios.findById(usuario.getId()).orElseThrow().getCredencialesVersion());
        assertThrows(BadRequestException.class, () -> passwords.restablecer(token, PASSWORD));
        assertThrows(UnauthorizedException.class, () -> auth.renovar(sesion.getRefreshToken()));
    }
    @Test void cambioExigePasswordActualYRevocaRefresh() {
        var sesion = login();
        assertThrows(UnauthorizedException.class, () -> auth.cambiarPassword(usuario.getEmail(), new ChangePasswordRequest("incorrecta", "NuevaPassword123!")));
        auth.cambiarPassword(usuario.getEmail(), new ChangePasswordRequest(PASSWORD, "NuevaPassword123!"));
        assertThrows(UnauthorizedException.class, () -> auth.renovar(sesion.getRefreshToken()));
    }
    @Test void correoInexistenteNoEnviaEmail() {
        passwords.solicitar("nadie@example.com");
        verifyNoInteractions(email);
    }

    private int get(String token) throws Exception {
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/usuarios/listar"))
                .header("Authorization", "Bearer " + token).GET().build();
        return java.net.http.HttpClient.newHttpClient().send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode();
    }
    @Test void filtroUsaRolesActualesYRechazaTokenRevocado() throws Exception {
        var sesion = login();
        assertEquals(403, get(sesion.getToken()));
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            Rol admin = new Rol(); admin.setRol(ERol.ADMINISTRADOR); em.persist(admin);
            em.find(Usuario.class, usuario.getId()).getRoles().add(admin);
        });
        assertEquals(200, get(sesion.getToken()));
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> em.find(Usuario.class, usuario.getId()).getRoles().clear());
        assertEquals(403, get(sesion.getToken()));
        auth.cambiarPassword(usuario.getEmail(), new ChangePasswordRequest(PASSWORD, "NuevaPassword123!"));
        assertEquals(401, get(sesion.getToken()));
        assertEquals(401, get(sesion.getRefreshToken()));
    }
    @Test void endpointsPublicosValidanDatosYRefreshNoDependeDelAccess() throws Exception {
        var sesion = login();
        var client = java.net.http.HttpClient.newHttpClient();
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/auth/refresh"))
                .header("Content-Type", "application/json").header("Authorization", "Bearer invalido")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"refreshToken\":\"" + sesion.getRefreshToken() + "\"}")).build();
        assertEquals(200, client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode());
        var invalid = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/auth/reset-password"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"token\":\"x\",\"newPassword\":\"123\"}")).build();
        assertEquals(400, client.send(invalid, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test void recuperacionVencidaNoCambiaPasswordYLimitaReenvios() {
        var enlace = new AtomicReference<String>();
        doAnswer(invocation -> { enlace.set(invocation.getArgument(1)); return null; }).when(email).enviarRecuperacion(eq(usuario.getEmail()), anyString(), anyLong());
        passwords.solicitar(usuario.getEmail());
        passwords.solicitar(usuario.getEmail());
        verify(email, times(1)).enviarRecuperacion(eq(usuario.getEmail()), anyString(), anyLong());
        String token = enlace.get().split("token=")[1];
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            resetTokens.findByTokenHash(TokenSeguro.hash(token)).orElseThrow().setFechaVencimiento(Instant.now().minusSeconds(1));
        });
        assertThrows(BadRequestException.class, () -> passwords.restablecer(token, "OtraPassword123!"));
        assertTrue(encoder.matches(PASSWORD, usuarios.findById(usuario.getId()).orElseThrow().getPassword()));
    }
}
