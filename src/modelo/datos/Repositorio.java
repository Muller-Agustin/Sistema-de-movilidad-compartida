package modelo.datos;

import java.util.List;

// DIP: el resto del sistema depende de esta interfaz y no de cómo se guardan los datos.
public interface Repositorio<T> {

    List<T> leerTodos();

    void guardarTodos(List<T> elementos);
}