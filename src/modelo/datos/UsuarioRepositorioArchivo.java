package modelo.datos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.cuentas.BilleteraVirtual;
import modelo.cuentas.MedioPago;
import modelo.cuentas.Movimiento;
import modelo.cuentas.Recarga;
import modelo.cuentas.PlanMensual;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.excepciones.PersistenciaExcepcion;

// Guarda los usuarios en tres archivos:
//   usuarios.txt    -> documento;nombre;email;fechaAlta;planFechaAlta;planMes;planViajesUsados
//   mediospago.txt  -> documento;tipo;dato1;dato2
//   movimientos.txt -> documento;tipo;fecha;importe;concepto;medio
// Al cargar, el saldo no se lee: se recalcula repitiendo los movimientos en orden, así el historial
// siempre explica el saldo y el estado del usuario (habilitado o no) queda como correspondía.
public class UsuarioRepositorioArchivo extends RepositorioArchivo<Usuario> {
    private static final String USUARIOS = "usuarios.txt";
    private static final String MEDIOS = "mediospago.txt";
    private static final String MOVIMIENTOS = "movimientos.txt";

    public UsuarioRepositorioArchivo(String carpeta) {
        super(carpeta);
    }

    @Override
    public List<Usuario> leerTodos() {
        List<Usuario> usuarios = leer(USUARIOS, c -> crearUsuario(c));
        Map<String, Usuario> porDocumento = new HashMap<>();
        for (Usuario u : usuarios) {
            porDocumento.put(u.getDocumento(), u);
        }
        recorrer(MEDIOS, c -> usuarioDe(porDocumento, c[0]).getCuenta().agregarMedioPago(crearMedio(c)));
        recorrer(MOVIMIENTOS, c -> aplicarMovimiento(usuarioDe(porDocumento, c[0]), c));
        return usuarios;
    }

    private Usuario crearUsuario(String[] c) {
        Usuario usuario = new Usuario(c[1], c[0], c[2], LocalDate.parse(c[3]));
        if (!esVacio(c[4])) {
            PlanMensual plan = new PlanMensual(LocalDate.parse(c[4]));
            YearMonth mes = YearMonth.parse(c[5]);
            int usados = Integer.parseInt(c[6]);
            for (int i = 0; i < usados; i++) {
                plan.registrarViaje(mes.atDay(1));
            }
            usuario.restaurarPlanMensual(plan);
        }
        return usuario;
    }

    private Usuario usuarioDe(Map<String, Usuario> porDocumento, String documento) {
        Usuario usuario = porDocumento.get(documento);
        if (usuario == null) {
            throw new PersistenciaExcepcion("No existe el usuario con documento " + documento);
        }
        return usuario;
    }

    private MedioPago crearMedio(String[] c) {
        switch (c[1]) {
            case "TARJETA":
                return new Tarjeta(c[2], c[3]);
            case "BILLETERA":
                return new BilleteraVirtual(c[2]);
            default:
                throw new IllegalArgumentException("Tipo de medio de pago desconocido: " + c[1]);
        }
    }

    private void aplicarMovimiento(Usuario usuario, String[] c) {
        LocalDateTime fecha = LocalDateTime.parse(c[2]);
        double importe = Double.parseDouble(c[3]);
        switch (c[1]) {
            case "RECARGA":
                usuario.recargarSaldo(importe, buscarMedio(usuario, c[5]), fecha);
                break;
            case "DEBITO":
                usuario.cobrar(importe, c[4], fecha);
                break;
            case "AJUSTE":
                usuario.ajustarSaldo(importe, c[4], fecha);
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento desconocido: " + c[1]);
        }
    }

    private MedioPago buscarMedio(Usuario usuario, String descripcion) {
        for (MedioPago medio : usuario.getCuenta().getMediosPago()) {
            if (medio.getDescripcion().equals(descripcion)) {
                return medio;
            }
        }
        throw new PersistenciaExcepcion("El usuario " + usuario.getDocumento() + " no tiene el medio de pago: " + descripcion);
    }

    @Override
    public void guardarTodos(List<Usuario> usuarios) {
        List<String> lineasUsuarios = new ArrayList<>();
        List<String> lineasMedios = new ArrayList<>();
        List<String> lineasMovimientos = new ArrayList<>();
        for (Usuario u : usuarios) {
            lineasUsuarios.add(lineaDeUsuario(u));
            for (MedioPago medio : u.getCuenta().getMediosPago()) {
                lineasMedios.add(lineaDeMedio(u, medio));
            }
            for (Movimiento m : u.getCuenta().getMovimientos()) {
                lineasMovimientos.add(lineaDeMovimiento(u, m));
            }
        }
        escribir(USUARIOS, lineasUsuarios);
        escribir(MEDIOS, lineasMedios);
        escribir(MOVIMIENTOS, lineasMovimientos);
    }

    private String lineaDeUsuario(Usuario u) {
        PlanMensual plan = u.getPlanMensual();
        if (plan == null) {
            return unir(u.getDocumento(), u.getNombre(), u.getEmail(), u.getFechaAlta(), VACIO, VACIO, VACIO);
        }
        YearMonth mes = plan.getMesDelContador();
        return unir(u.getDocumento(), u.getNombre(), u.getEmail(), u.getFechaAlta(),
                plan.getFechaAlta(), mes, plan.getViajesUsados(mes.atDay(1)));
    }

    private String lineaDeMedio(Usuario u, MedioPago medio) {
        if (medio instanceof Tarjeta) {
            Tarjeta t = (Tarjeta) medio;
            return unir(u.getDocumento(), "TARJETA", t.getTitular(), t.getUltimosDigitos());
        }
        if (medio instanceof BilleteraVirtual) {
            return unir(u.getDocumento(), "BILLETERA", ((BilleteraVirtual) medio).getAlias(), VACIO);
        }
        throw new PersistenciaExcepcion("Medio de pago no soportado: " + medio.getDescripcion());
    }

    private String lineaDeMovimiento(Usuario u, Movimiento m) {
        String descripcionMedio = VACIO;
        if (m instanceof Recarga) {
            descripcionMedio = ((Recarga) m).getMedio().getDescripcion();
        }
        return unir(u.getDocumento(), m.getTipo(), m.getFecha(), m.getImporte(), m.getConcepto(), descripcionMedio);
    }
}