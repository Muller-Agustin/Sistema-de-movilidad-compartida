package pruebas;

// Ayudante para las pruebas: compara el resultado esperado con el obtenido,
// imprime OK o FALLA y cuenta cuántas pruebas pasaron.
public class Verificador {

    // Una acción que se espera que lance una excepción
    public interface Accion {
        void ejecutar();
    }

    private int exitosas;
    private int fallidas;

    public void verificarVerdadero(String nombre, boolean condicion) {
        informar(nombre, condicion, "esperado: true, obtenido: " + condicion);
    }

    public void verificarFalso(String nombre, boolean condicion) {
        informar(nombre, !condicion, "esperado: false, obtenido: " + condicion);
    }

    public void verificarNumero(String nombre, double esperado, double obtenido) {
        boolean iguales = Math.abs(esperado - obtenido) < 0.001;
        informar(nombre, iguales, "esperado: " + esperado + ", obtenido: " + obtenido);
    }

    // para números con decimales (distancias): pasa si la diferencia es menor a la tolerancia
    public void verificarAproximado(String nombre, double esperado, double obtenido, double tolerancia) {
        boolean iguales = Math.abs(esperado - obtenido) < tolerancia;
        informar(nombre, iguales, "esperado: " + esperado + " (+/- " + tolerancia + "), obtenido: " + obtenido);
    }

    public void verificarIgual(String nombre, Object esperado, Object obtenido) {
        boolean iguales = esperado.equals(obtenido);
        informar(nombre, iguales, "esperado: " + esperado + ", obtenido: " + obtenido);
    }

    // La prueba pasa si la acción lanza una excepción del tipo indicado
    public void verificarExcepcion(String nombre, Class<? extends Exception> tipo, Accion accion) {
        try {
            accion.ejecutar();
            informar(nombre, false, "esperado: " + tipo.getSimpleName() + ", pero no se lanzó ninguna");
        } catch (Exception e) {
            informar(nombre, tipo.isInstance(e),
                    "esperado: " + tipo.getSimpleName() + ", obtenido: " + e.getClass().getSimpleName());
        }
    }

    private void informar(String nombre, boolean paso, String detalle) {
        if (paso) {
            exitosas++;
            System.out.println("OK    " + nombre);
        } else {
            fallidas++;
            System.out.println("FALLA " + nombre + " -> " + detalle);
        }
    }

    public void resumen() {
        System.out.println();
        System.out.println("Pruebas exitosas: " + exitosas + " | fallidas: " + fallidas);
    }

    public int getFallidas() {
        return fallidas;
    }
}