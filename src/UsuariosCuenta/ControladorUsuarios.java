package UsuariosCuenta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Excepciones.SaldoInsuficienteExcepcion;

// Controlador (GRASP) del módulo de usuarios: alta y búsqueda de usuarios,
// medios de pago, recargas de saldo y suscripción al plan mensual.

public class ControladorUsuarios implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Usuario> usuarios;

    public ControladorUsuarios() {
        this.usuarios = new ArrayList<>();
    }

    // RF01: falla si ya existe un usuario con ese documento
    public void registrarUsuario(Usuario usuario) {
        if (buscarUsuario(usuario.getDocumento()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con el documento " + usuario.getDocumento());
        }
        usuarios.add(usuario);
    }

    // Devuelve null si no existe
    public Usuario buscarUsuario(String documento) {
        for (Usuario u : usuarios) {
            if (u.getDocumento().equals(documento)) {
                return u;
            }
        }
        return null;
    }

    // RF02
    public void agregarMedioPago(Usuario usuario, MedioPago medio) {
        usuario.getCuenta().agregarMedioPago(medio);
    }

    // RF03
    public void recargarSaldo(Usuario usuario, double importe, MedioPago medio) {
        if (importe <= 0) {
            throw new IllegalArgumentException("El importe de la recarga debe ser mayor a cero");
        }
        usuario.recargarSaldo(importe, medio, LocalDateTime.now());
    }

    // RF05
    public void suscribirPlan(Usuario usuario) throws SaldoInsuficienteExcepcion {
        usuario.suscribirPlan(LocalDateTime.now());
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }
}