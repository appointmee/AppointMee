
# AppointMee
Gestor de turnos para Trabajo Practico Final de la Materia Programacion III en la Universidad Tecnologica Nacional


## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Mercado Pago SDK
* Maven

## Seguridad

* Spring Security
* Autenticación basada en sesiones
* Control de acceso por roles

## Base de datos
* PostgreSQL (SupaBase)

El proyecto no usa migraciones automáticas (`spring.jpa.hibernate.ddl-auto=validate`),
por lo que los scripts de `backend/src/main/resources/db/` hay que correrlos a mano en la base.
Por ejemplo, `db/sucursal_dias_apertura.sql` agrega la columna con los días de la semana
que abre cada sucursal (los días que no figuran se toman como cerrados).
   
## Frontend

* HTML5
* CSS3
* JavaScript
* Tailwind
* Bootstrap

## Deploy

* Render
* Docker

## Cuentas de Prueba

### Administrador

**Email:** [admin123@gmail.com](mailto:admin123@gmail.com)
**Contraseña:** Hola12_!

### Empleador

**Email:** [gonzalo@gmail.com](mailto:gonzalo@gmail.com)
**Contraseña:** Hola12_!

### Empleado

**Email:** [claudio@gmail.com](mailto:claudio@gmail.com)
**Contraseña:** Hola12_!

### Cliente

No se incluyen cuentas de prueba para clientes. (Tanto el empleado y empleador ya mencionados son clientes)

Para probar las funcionalidades de cliente, registrar un nuevo usuario desde la aplicación.




### Instrucciones

1. Clonar el repositorio y acceder al directorio:
   ```bash
   git clone https://github.com/gonnnzaDev/AppointMee.git
   cd AppointMee
   ```

2. Configurar la base de datos en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.show-sql=true
   spring.datasource.username= (Usuario)
   spring.datasource.password= (Contraseña)
   spring.datasource.url= (Bdd)
   ```

3. Compilar y ejecutar:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Acceder a la aplicación en `https://appointmee-vcs2.onrender.com`

# Equipo
* Backend : **Giovanni Morro Bai**.
* Frontend : **Gonzalo Leonel Lopez**.

