package com.gg.turnlook.features.solicitud.empleado;

import com.gg.turnlook.common.exception.ConflictException;
import com.gg.turnlook.common.exception.ForbiddenException;
import com.gg.turnlook.common.exception.NotFoundException;
import com.gg.turnlook.features.resenia.ReseniaService;
import com.gg.turnlook.features.solicitud.EstadoSolicitud;
import com.gg.turnlook.features.solicitud.empleado.dto.SolicitudEmpleadoMiniPropiaDTO;
import com.gg.turnlook.features.solicitud.empleado.dto.SolicitudEmpleadoMiniSucursalDTO;
import com.gg.turnlook.features.solicitud.empleado.dto.SolicitudEmpleadoResponseDTO;
import com.gg.turnlook.features.solicitud.empleador.dto.SolicitudEmpleadorMiniDTO;
import com.gg.turnlook.features.sucursal.Sucursal;
import com.gg.turnlook.features.sucursal.SucursalService;
import com.gg.turnlook.features.sucursal.dto.SucursalMiniDTO;
import com.gg.turnlook.features.usuario.Usuario;
import com.gg.turnlook.features.usuario.UsuarioService;
import com.gg.turnlook.features.usuario.dto.UsuarioResponseDTO;
import com.gg.turnlook.features.usuario.rol.ERol;
import com.gg.turnlook.features.usuario.rol.Rol;
import com.gg.turnlook.features.usuario.rol.RolService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;





@Service
public class SolicitudEmpleadoService {


    private final SolicitudEmpleadoRepository solicitudEmpleadoRepo;
    private final SucursalService sucursalService;
    private final ReseniaService reseniaService;
    private final RolService rolService;
    private final UsuarioService usuarioService;


    public SolicitudEmpleadoService(SolicitudEmpleadoRepository solicitudEmpleadoRepo, SucursalService sucursalService, ReseniaService reseniaService, RolService rolService, UsuarioService usuarioService) {
        this.solicitudEmpleadoRepo = solicitudEmpleadoRepo;
        this.sucursalService = sucursalService;
        this.reseniaService = reseniaService;
        this.rolService = rolService;
        this.usuarioService = usuarioService;
    }


    /// METODOS



    public List<SolicitudEmpleadoMiniSucursalDTO> listarSolicitudesEnviadas(
            Integer sucursalId, String empleadorEmail) {

        Sucursal sucursal = sucursalService.listarSucursalPorId(sucursalId);

        if (!sucursalService.esEmpleadorAca(sucursal, empleadorEmail)) {
            throw new ForbiddenException("No tenes permisos");
        }

        return solicitudEmpleadoRepo.findBySucursalId(sucursal.getId()).stream()
                .map(sol -> new SolicitudEmpleadoMiniSucursalDTO(
                        sol.getId(), sol.getEmpleado().getNombre(),
                        sol.getEmpleado().getApellido(), sol.getFechaSolicitud())
                ).toList();
    }


    public List<SolicitudEmpleadoMiniPropiaDTO> listarSolicitudesRecibidas(
            String userEmail) {

        return solicitudEmpleadoRepo.findByEmpleadoEmail(userEmail)
                .stream()
                .map(sol -> new SolicitudEmpleadoMiniPropiaDTO(
                        sol.getId(), sol.getSucursal().getNombre(), sol.getFechaSolicitud())
                ).toList();
    }


    public SolicitudEmpleadoResponseDTO verDetallesSolicitud(
            Integer solicitudId, String userEmail) {

        SolicitudEmpleado solicitud = listarSolicitudPorId(solicitudId);

        if (!solicitud.getEmpleado().getEmail().equals(userEmail) &&
                !solicitud.getSucursal().getEmpleador().getEmail().equals(userEmail)) {
            throw new ForbiddenException("No tenes permisos");
        }

        return mapearSolicitud(solicitud);
    }


    private SolicitudEmpleadoResponseDTO mapearSolicitud(SolicitudEmpleado solicitud) {

        SolicitudEmpleadoResponseDTO solicitudDTO = new SolicitudEmpleadoResponseDTO();

        solicitudDTO.setId(solicitud.getId());

        solicitudDTO.setEstadoSolicitud(solicitud.getEstado());

        solicitudDTO.setFechaSolicitud(solicitud.getFechaSolicitud());

        Usuario e = solicitud.getEmpleado();
        UsuarioResponseDTO empleado = new UsuarioResponseDTO(
                e.getId(), e.getNombre(), e.getApellido(),
                e.getEmail(), e.getFotoPerfil().getFotoValida());

        solicitudDTO.setEmpleado(empleado);

        Sucursal s = solicitud.getSucursal();
        SucursalMiniDTO sucursal = new SucursalMiniDTO(
                s.getId(), s.getNombre(), s.getCategoria().getCategoria().name(),
                s.getFotoPerfil().getFotoValida(),
                reseniaService.getPuntuacionPromedioSucursal(s.getId()),
                reseniaService.getPuntuacionesTotalesSucursal(s.getId()));

        solicitudDTO.setSucursal(sucursal);

        return solicitudDTO;
    }


    @Transactional
    public void aceptarSolicitud(Integer solicitudId, String userEmail) {

        SolicitudEmpleado solicitud = listarSolicitudPorId(solicitudId);

        if (!solicitud.getEmpleado().getEmail().equals(userEmail)) {
            throw new ForbiddenException("No tenes permisos");
        }

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictException("Solo se pueden aceptar solicitudes pendientes");
        }

        Rol empleado = rolService.listarPorRol(ERol.EMPLEADO);

        usuarioService.agregarRol(solicitud.getEmpleado(), empleado);

        sucursalService.guardarEmpleado(solicitud.getEmpleado(), solicitud.getSucursal());

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitudEmpleadoRepo.save(solicitud);
    }


    public void rechazarSolicitud(Integer solicitudId, String userEmail) {

        SolicitudEmpleado solicitud = listarSolicitudPorId(solicitudId);

        if (!solicitud.getEmpleado().getEmail().equals(userEmail)) {
            throw new ForbiddenException("No tenes permisos");
        }

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictException("Solo se pueden rechazar solicitudes pendientes");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudEmpleadoRepo.save(solicitud);
    }


    private SolicitudEmpleado listarSolicitudPorId(Integer solicitudId) {

        return solicitudEmpleadoRepo.findById(solicitudId)
                .orElseThrow(() -> new NotFoundException("No se encontró la solicitud"));
    }


}





