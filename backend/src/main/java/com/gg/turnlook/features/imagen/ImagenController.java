package com.gg.turnlook.features.imagen;

import com.gg.turnlook.features.imagen.dto.ImagenSubidaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/imagenes")
public class ImagenController {

    private final ImagenCloudinaryService imagenCloudinaryService;

    public ImagenController(ImagenCloudinaryService imagenCloudinaryService) {
        this.imagenCloudinaryService = imagenCloudinaryService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public ResponseEntity<ImagenSubidaDTO> subir(
            @RequestParam("file") MultipartFile archivo,
            @RequestParam ImagenCloudinaryTipo tipo
    ) {
        return ResponseEntity.ok(imagenCloudinaryService.subir(archivo, tipo));
    }
}
