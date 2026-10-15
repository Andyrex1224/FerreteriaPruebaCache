# Ferretería con caché

Este proyecto muestra cuánto más rápido es leer datos desde una **caché** (la memoria del programa) que pedirlos cada vez a la **base de datos**.

Tiene una lista de **10.000 productos** de ferretería. Desde una página web puedes consultarlos de dos formas y comparar los tiempos.

---

## ¿Cómo funciona el código?

El proyecto tiene 3 partes:

### 1. La base de datos
- Se usa **H2**, una base de datos que vive **dentro del programa Java**. No hay que instalar MySQL.
- Cada vez que arrancas el programa, la base se crea sola con estos archivos:
  - `schema.sql` crea la tabla `productos` (id, nombre, precio).
  - `data.sql` mete los 10.000 productos.
- Al cerrar el programa la base se borra, y se vuelve a crear la próxima vez.

### 2. El backend (`Main.java`)
Es el programa en Java (Spring Boot). Recibe las peticiones de la página web y responde con los productos.

- **Sin caché** (`/api/productos?cache=false`): siempre le pregunta a la base de datos. Es más lento.
- **Con caché** (`/api/productos`):
  - La **primera vez** le pregunta a la base de datos y guarda la lista en memoria.
  - Las **siguientes veces** devuelve la lista guardada, sin ir a la base. Es mucho más rápido.
- **Buscar por ID** (`/api/productos/5`): busca un solo producto y también lo guarda en memoria.

En cada respuesta el backend dice cuánto tardó, en milisegundos.

### 3. La página web (`static/index.html`)
- Tiene botones para consultar **sin caché**, **con caché** y **por ID**.
- Muestra el tiempo de cada consulta y una barra para comparar cuál fue más rápida.
- Muestra los productos en una tabla.

---

## ¿Cómo se ejecuta?

Solo necesitas tener **Java 21 o superior** instalado.

### Opción A: con IntelliJ
1. Abre IntelliJ y ve a **File → Open**. Elige la carpeta `FerreteriaCacheH2`.
2. Espera a que termine de cargar. La primera vez descarga cosas y tarda un poco.
3. Abre el archivo `src/main/java/com/ferreteria/Main.java`.
4. Presiona el botón verde ▶️.
5. Cuando abajo aparezca `Started Main`, ya está listo.

### Opción B: con la terminal
1. Abre una terminal dentro de la carpeta `FerreteriaCacheH2`.
2. Escribe:
   ```
   .\mvnw.cmd spring-boot:run
   ```
3. Cuando aparezca `Started Main`, ya está listo.

### Abrir la página
Entra en el navegador a: **http://localhost:8080**

Prueba primero **Ejecutar sin caché**, luego **Ejecutar con caché** dos veces, y compara los tiempos.

---

## ¿Cómo se detiene?
- **En IntelliJ:** el botón rojo ⏹️.
- **En la terminal:** `Ctrl + C`.

## Problema común
**"Port 8080 was already in use"**: el programa ya está corriendo en otro lado, por ejemplo en IntelliJ y en la terminal al mismo tiempo. Detén uno y deja solo el otro.
