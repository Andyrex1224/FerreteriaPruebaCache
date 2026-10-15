-- Estructura de la base de datos de la ferretería (H2 embebida, en memoria).
-- Spring Boot ejecuta este archivo automáticamente al arrancar, antes de data.sql.
DROP TABLE IF EXISTS productos;
CREATE TABLE productos (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id)
);
