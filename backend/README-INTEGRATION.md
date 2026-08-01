MediaHub Integrated

This workspace aggregates two existing modules and a lightweight gateway for local development.

Modules:
- contentcatalog_git_individual (content catalog service) — runs on port 8093
- mediahub-combined/combined (IAM + audit module) — runs on port 8091
- gateway (lightweight proxy) — runs on port 8080 and proxies routes:
  - /content/** -> contentcatalog
  - /media/** -> mediahub

Quick run (from workspace root):

1) Start all services with Maven wrappers:

```bash
.\run-all.ps1
```

This opens 3 console windows, one for each service.

2) Alternatively, run each module individually in separate terminals:

```bash
# Terminal 1: Content Catalog
cd contentcatalog_git_individual
.\mvnw.cmd spring-boot:run

# Terminal 2: MediaHub IAM/Audit
cd mediahub-combined\combined
.\mvnw.cmd spring-boot:run

# Terminal 3: Gateway
cd gateway
.\mvnw.cmd spring-boot:run
```

3) Access services:
- Gateway: http://localhost:8080/content/... or /media/...
- Direct Content Catalog: http://localhost:8093/...
- Direct MediaHub: http://localhost:8091/...

Notes:
- Ports are configured in each module's `application.properties`.
- All three modules share a single MySQL database at localhost:3306/mediahub.
- Update DB credentials in each service's `application.properties` if needed.
