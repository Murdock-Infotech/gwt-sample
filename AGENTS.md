# AGENTS.md

This file is a **working guide** for humans and automated agents contributing to this repo. It summarizes the project structure, how to run/debug it locally, and the constraints that matter (GWT + Spring 4.x + `javax.*`).

## Architecture at a glance

- **Frontend**: GWT (SuperDevMode / CodeServer supported)
- **Backend**: Spring MVC (non-Boot) running on **Jetty** via `jetty-maven-plugin`
- **Data**: Spring Data JPA + Hibernate + **H2 in-memory**
- **Java**: **11**

### Maven modules

- `modular-webapp-shared/`  
  Shared DTOs + GWT RPC service interfaces. Packaged as a JAR that **includes `.java` and `.gwt.xml`** so GWT can compile translatable sources.

- `modular-webapp-client/`  
  GWT client module `murdockinfotech.client.ModularWebapp` (module short name **`modularwebapp`**). SuperDevMode scripts live here.

- `modular-webapp-server/`  
  WAR module run with Jetty. Serves `index.html`, static assets, the GWT compiled output, and the GWT RPC servlet.

## Key runtime ports / URLs

- **Backend (Jetty)**: `http://localhost:8080/`
  - Port is controlled by `modular-webapp-server/pom.xml` property `jetty.http.port` (default **8080**).
  - Stop port default **9999**.

- **GWT CodeServer (SuperDevMode)**: `http://localhost:9876/`
  - Manual recompile endpoint: `http://localhost:9876/recompile/modularwebapp`

## How to run locally

### Build everything

From repo root:

```bash
mvn -DskipTests package
```

If the browser shows a 404 for `/modularwebapp/modularwebapp.nocache.js`, ensure the client has been built (the Jetty config serves `modular-webapp-client/war` as a resource base).

### Start backend (Jetty)

From repo root:

```bash
mvn -pl modular-webapp-server jetty:run
```

Then open:
- `http://localhost:8080/`

### Start GWT SuperDevMode (CodeServer)

From `modular-webapp-client/`:

```bash
./devModeWithParams.sh
```

Windows / PowerShell:

```powershell
./devModeWithParams.ps1
```

Then open the app with CodeServer enabled:
- `http://localhost:8080/?gwt.codesvr=localhost:9876`

### Auto-recompile on file changes

This triggers `GET http://localhost:9876/recompile/modularwebapp` when sources change in:
- `modular-webapp-client/src/main/java`
- `modular-webapp-shared/src/main/java`

macOS (requires `fswatch`):

```bash
cd modular-webapp-client && ./autoRecompile.sh
```

PowerShell (no extra deps):

```powershell
cd modular-webapp-client; pwsh ./autoRecompile.ps1
```

## Important endpoints / mappings

- **Host page**: `modular-webapp-server/src/main/webapp/index.html`
  - Loads runtime config from `GET /client-config.js`
  - Loads GWT bootstrap from `/modularwebapp/modularwebapp.nocache.js`
  - When `?gwt.codesvr=...` is present, it passes that through to the bootstrap script.

- **Runtime client config**: `GET /client-config.js`  
  Implemented by `murdockinfotech.server.controller.ClientConfigController`. It emits:
  - `window.__MODULAR_WEBAPP_CONTEXT_ROOT__ = 'http://localhost:8080'` (configurable via `modularwebapp.contextRoot` in `application.properties`)

- **GWT RPC servlet**: `POST /modularwebapp/userService`  
  Mapped in `modular-webapp-server/src/main/webapp/WEB-INF/web.xml` to `murdockinfotech.server.UserServiceImpl`.

## Spring / JPA / H2 wiring

- Root context: `murdockinfotech.server.config.AppRootConfig`
  - Loads `classpath:application.properties`
  - Imports `classpath:applicationContext.xml`

- JPA/H2: configured in `modular-webapp-server/src/main/resources/applicationContext.xml`
  - H2 URL: `jdbc:h2:mem:modularwebapp;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false`
  - `hibernate.hbm2ddl.auto=create-drop` (schema recreated each run)
  - Entities are scanned from: `murdockinfotech.server.domain`
  - Spring Data repositories from: `murdockinfotech.server.repository`

## Debugging

### Backend Java breakpoints (Jetty remote debug)

The `modular-webapp-server/README.md` documents VS Code / Cursor launch configs that start Jetty with **JDWP on :5005** and attach automatically.

If you want to run it manually with JDWP:

```bash
export MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
mvn -pl modular-webapp-server jetty:run
```

## Constraints / “don’t accidentally break this”

- **Keep Java 11 compatibility**.
- **This is `javax.*` land** (Spring 4.3 + Jetty 9.4 + GWT RPC `RemoteServiceServlet`).
  - Avoid migrating to Jakarta (`jakarta.servlet.*`, `jakarta.persistence.*`) unless you’re intentionally upgrading the entire stack.
- **Shared module packaging is intentional**: `modular-webapp-shared` includes `.java` sources to support GWT compilation.

## Where to look when changing things

- **GWT module descriptors**: `modular-webapp-client/src/main/java/**.gwt.xml` and `modular-webapp-shared/src/main/java/**.gwt.xml`
- **RPC interfaces**: `modular-webapp-shared/src/main/java/murdockinfotech/shared/service/`
- **Servlet mappings**: `modular-webapp-server/src/main/webapp/WEB-INF/web.xml`
- **Web MVC controllers**: `modular-webapp-server/src/main/java/murdockinfotech/server/controller/`
- **JPA entities / repos**:
  - `modular-webapp-server/src/main/java/murdockinfotech/server/domain/`
  - `modular-webapp-server/src/main/java/murdockinfotech/server/repository/`
