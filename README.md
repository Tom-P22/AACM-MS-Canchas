🏟️ Microservicio de Canchas Municipales (ms-canchas)

Este microservicio forma parte del ecosistema de la plataforma municipal y está diseñado bajo una arquitectura de microservicios utilizando **Spring Boot 3**. Su propósito principal es gestionar los recintos deportivos y las canchas disponibles de la municipalidad, permitiendo su creación, actualización, eliminación y consultas avanzadas.

Está preparado para integrarse de forma reactiva con el microservicio de Pagos (`ms-pagos`) mediante `WebClient`.

---

## 🛠️ Tecnologías y Herramientas Utilizadas

* **Java 21**
* **Spring Boot 3.x** (Spring Data JPA, Spring Web)
* **MySQL 8.0** (Corriendo sobre contenedor Docker dedicado)
* **Lombok** (Para la reducción de código repetitivo)
* **Jakarta Validation** (Para la validación de DTOs en las peticiones)

---

## ⚙️ Requisitos Previos y Configuración del Entorno

### 1. Levantar la Base de Datos en Docker
Para evitar conflictos de puertos con otros microservicios del ecosistema, este servicio utiliza de forma dedicada el puerto **3308** local mapeado al puerto nativo de MySQL.

Ejecuta el siguiente comando en tu terminal para levantar el contenedor:
```bash
docker run --name mysql-canchas -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=db_canchas -p 3308:3306 -d mysql:8.0
2. Datos Iniciales (Seeders)El sistema cuenta con un archivo src/main/resources/import.sql que poblará de forma automática la tabla recinto con datos de prueba la primera vez que se inicialice el servicio bajo el perfil de desarrollo, asegurando la existencia del recintoId: 1.🚀 Endpoints Disponibles (API REST)El microservicio escucha de manera local en el puerto 8081 (http://localhost:8081).🔹 Gestión de Canchas (/api/v1/cancha)MétodoEndpointDescripciónBody (JSON) requeridoPOST/api/v1/canchaRegistrar una nueva canchaSí (Ver ejemplo abajo)GET/api/v1/canchaListar todas las canchas registradasNoGET/api/v1/cancha/{id}Obtener el detalle de una cancha por IDNoPUT/api/v1/cancha/{id}Actualizar datos de una canchaSíDELETE/api/v1/cancha/{id}Eliminar lógicamente una canchaNo🔹 Búsquedas y Filtros PersonalizadosGET /api/v1/cancha/buscar?texto={nombre} -> Busca canchas filtrando por coincidencia en el nombre.GET /api/v1/cancha/recinto/{recintoId} -> Lista todas las canchas asociadas a un recinto municipal específico.GET /api/v1/cancha/baja-capacidad?max={cantidad} -> Filtra canchas cuya capacidad de jugadores sea menor o igual al parámetro introducido.📝 Ejemplo de Petición para Creación (POST)URL: http://localhost:8081/api/v1/canchaHeaders: Content-Type: application/jsonFormato de Fecha requerido: dd-MM-yyyyJSON{
    "nombre": "Cancha Principal de Pasto Sintético",
    "tipoDeCancha": "Fútbol 11",
    "fechaRegistro": "16-05-2026",
    "direccion": "Av. Las Torres 450",
    "recintoId": 1,
    "capacidad": 22
}
🔐 Estado de la Seguridad (JWT)⚠️ Nota de Desarrollo: Los bloques de interceptores de Spring Security y el contexto de auditoría por SecurityContextHolder se encuentran comentados temporalmente en la capa de servicios para facilitar las pruebas locales y la integración directa con el WebClient. Se activarán globalmente junto a la clave secreta jwt.secret en la fase final de despliegue.
