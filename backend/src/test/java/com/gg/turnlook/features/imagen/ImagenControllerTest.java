package com.gg.turnlook.features.imagen;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gg.turnlook.features.imagen.dto.ImagenSubidaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ImagenControllerTest {

    private ImagenCloudinaryService cloudinaryService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        cloudinaryService = mock(ImagenCloudinaryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ImagenController(cloudinaryService)).build();
    }

    @Test
    void endpointRecibeMultipartYDevuelveUrlPublicIdYNombre() throws Exception {
        when(cloudinaryService.subir(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(ImagenCloudinaryTipo.PERFIL)))
                .thenReturn(new ImagenSubidaDTO(
                        "https://res.cloudinary.com/cloud/image/upload/appointmee/perfiles/foto.jpg",
                        "appointmee/perfiles/foto", "foto.jpg"));

        mockMvc.perform(multipart("/imagenes/upload")
                        .file(new MockMultipartFile("file", "foto.jpg", "image/jpeg",
                                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}))
                        .param("tipo", "PERFIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/cloud/image/upload/appointmee/perfiles/foto.jpg"))
                .andExpect(jsonPath("$.publicId").value("appointmee/perfiles/foto"))
                .andExpect(jsonPath("$.nombreArchivo").value("foto.jpg"));

        verify(cloudinaryService).subir(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(ImagenCloudinaryTipo.PERFIL));
    }

    @Test
    void endpointRechazaTipoNoPermitido() throws Exception {
        mockMvc.perform(multipart("/imagenes/upload")
                        .file(new MockMultipartFile("file", "foto.jpg", "image/jpeg",
                                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}))
                        .param("tipo", "ADMIN"))
                .andExpect(status().isBadRequest());
    }
}
