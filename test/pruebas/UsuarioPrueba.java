package pruebas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import modelo.cuentas.EstadoUsuario;
import modelo.cuentas.MedioPago;
import modelo.cuentas.Tarjeta;
import modelo.cuentas.Usuario;
import modelo.excepciones.RetiroNoPermitidoExcepcion;
import modelo.excepciones.SaldoInsuficienteExcepcion;

public class UsuarioPrueba {
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 9, 10, 0);
    private static final LocalDate ALTA = LocalDate.of(2026, 11, 1);

    public static void ejecutar(Verificador v) {
        unUsuarioNuevoEstaHabilitadoYConSaldoCero(v);
        noSeCreaUnUsuarioConDatosInvalidos(v);
        puedeRetirarSoloSiAlcanzaElSaldoMinimo(v);
        validarRetiroExplicaPorQueNoPuede(v);
        siElSaldoQuedaNegativoElUsuarioSeInhabilita(v);
        unUsuarioInhabilitadoNoPuedeRetirar(v);
        alRegularizarElSaldoElUsuarioSeRehabilita(v);
        recargarSinLlegarACeroNoRehabilita(v);
        suscribirseAlPlanCobraElPrecio(v);
        noSePuedeSuscribirSinSaldoNiDosVeces(v);
        laIdentificacionIncluyeNombreYDocumento(v);
    }

    // Crea un usuario con una tarjeta ya registrada
    private static Usuario crearUsuario(MedioPago tarjeta) {
        Usuario usuario = new Usuario("Ana", "12345678", "ana@mail.com", ALTA);
        usuario.getCuenta().agregarMedioPago(tarjeta);
        return usuario;
    }

    private static void unUsuarioNuevoEstaHabilitadoYConSaldoCero(Verificador v) {
        Usuario usuario = crearUsuario(new Tarjeta("Ana", "1234"));
        v.verificarVerdadero("Usuario: nuevo está habilitado", usuario.estaHabilitado());
        v.verificarNumero("Usuario: nuevo tiene saldo 0", 0, usuario.getCuenta().getSaldo());
    }

    private static void noSeCreaUnUsuarioConDatosInvalidos(Verificador v) {
        v.verificarExcepcion("Usuario: nombre vacío falla", IllegalArgumentException.class,
                () -> new Usuario("", "1", "a@b.com", ALTA));
        v.verificarExcepcion("Usuario: documento vacío falla", IllegalArgumentException.class,
                () -> new Usuario("Ana", " ", "a@b.com", ALTA));
        v.verificarExcepcion("Usuario: mail sin arroba falla", IllegalArgumentException.class,
                () -> new Usuario("Ana", "1", "sin-arroba", ALTA));
    }

    private static void puedeRetirarSoloSiAlcanzaElSaldoMinimo(Verificador v) {
        // RN01 + S1: mínimo de una bicicleta común = 200 + 30 * 20 = 800
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        Usuario usuario = crearUsuario(tarjeta);

        usuario.recargarSaldo(799, tarjeta, FECHA);
        v.verificarFalso("Usuario: con 799 no puede retirar (mínimo 800)", usuario.puedeRetirar(800));

        usuario.recargarSaldo(1, tarjeta, FECHA);
        v.verificarVerdadero("Usuario: con 800 sí puede retirar", usuario.puedeRetirar(800));
    }

    private static void validarRetiroExplicaPorQueNoPuede(Verificador v) {
        Usuario usuario = crearUsuario(new Tarjeta("Ana", "1234"));
        try {
            usuario.validarRetiro(800);
            v.verificarVerdadero("Usuario: sin saldo el retiro lanza excepción", false);
        } catch (RetiroNoPermitidoExcepcion e) {
            v.verificarVerdadero("Usuario: el mensaje explica que falta saldo",
                    e.getMessage().contains("Saldo insuficiente"));
        }
    }

    private static void siElSaldoQuedaNegativoElUsuarioSeInhabilita(Verificador v) {
        // RN03: 500 - 800 = -300
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        Usuario usuario = crearUsuario(tarjeta);
        usuario.recargarSaldo(500, tarjeta, FECHA);
        usuario.cobrar(800, "Viaje", FECHA);

        v.verificarNumero("Usuario: 500 - 800 deja saldo -300", -300, usuario.getCuenta().getSaldo());
        v.verificarIgual("Usuario: con saldo negativo queda inhabilitado",
                EstadoUsuario.INHABILITADO, usuario.getEstado());
        v.verificarFalso("Usuario: inhabilitado no puede retirar", usuario.puedeRetirar(0));
    }

    private static void unUsuarioInhabilitadoNoPuedeRetirar(Verificador v) {
        Usuario usuario = crearUsuario(new Tarjeta("Ana", "1234"));
        usuario.cobrar(100, "Viaje", FECHA);
        v.verificarExcepcion("Usuario: inhabilitado no retira ni con mínimo 0",
                RetiroNoPermitidoExcepcion.class, () -> usuario.validarRetiro(0));
    }

    private static void alRegularizarElSaldoElUsuarioSeRehabilita(Verificador v) {
        // S12: -300 + 500 = 200 (cero o más) => habilitado
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        Usuario usuario = crearUsuario(tarjeta);
        usuario.cobrar(300, "Viaje", FECHA);
        v.verificarIgual("Usuario: con -300 está inhabilitado",
                EstadoUsuario.INHABILITADO, usuario.getEstado());

        usuario.recargarSaldo(500, tarjeta, FECHA);
        v.verificarNumero("Usuario: -300 + 500 deja saldo 200", 200, usuario.getCuenta().getSaldo());
        v.verificarIgual("Usuario: con saldo 200 se rehabilita",
                EstadoUsuario.HABILITADO, usuario.getEstado());
    }

    private static void recargarSinLlegarACeroNoRehabilita(Verificador v) {
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        Usuario usuario = crearUsuario(tarjeta);
        usuario.cobrar(300, "Viaje", FECHA);
        usuario.recargarSaldo(100, tarjeta, FECHA);
        v.verificarNumero("Usuario: -300 + 100 deja saldo -200", -200, usuario.getCuenta().getSaldo());
        v.verificarIgual("Usuario: con saldo -200 sigue inhabilitado",
                EstadoUsuario.INHABILITADO, usuario.getEstado());
    }

    private static void suscribirseAlPlanCobraElPrecio(Verificador v) {
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        Usuario usuario = crearUsuario(tarjeta);
        usuario.recargarSaldo(10000, tarjeta, FECHA);
        usuario.suscribirPlan(FECHA);

        v.verificarNumero("Usuario: 10000 - 8000 del plan deja 2000", 2000, usuario.getCuenta().getSaldo());
        v.verificarNumero("Usuario: el plan nuevo tiene 20 viajes", 20,
                usuario.getPlanMensual().getViajesRestantes(FECHA.toLocalDate()));
    }

    private static void noSePuedeSuscribirSinSaldoNiDosVeces(Verificador v) {
        MedioPago tarjeta = new Tarjeta("Ana", "1234");
        Usuario usuario = crearUsuario(tarjeta);
        v.verificarExcepcion("Usuario: suscribirse sin saldo falla", SaldoInsuficienteExcepcion.class,
                () -> usuario.suscribirPlan(FECHA));

        usuario.recargarSaldo(20000, tarjeta, FECHA);
        usuario.suscribirPlan(FECHA);
        v.verificarExcepcion("Usuario: suscribirse dos veces falla", IllegalStateException.class,
                () -> usuario.suscribirPlan(FECHA));
    }

    private static void laIdentificacionIncluyeNombreYDocumento(Verificador v) {
        Usuario usuario = crearUsuario(new Tarjeta("Ana", "1234"));
        v.verificarIgual("Usuario: identificación para reportes",
                "Usuario Ana (documento 12345678)", usuario.getIdentificacion());
    }

    public static void main(String[] args) {
        Verificador v = new Verificador();
        ejecutar(v);
        v.resumen();
    }
}