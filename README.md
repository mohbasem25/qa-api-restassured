# qa-api-restassured

[![CI](https://github.com/mohbasem25/qa-api-restassured/actions/workflows/api-tests.yml/badge.svg)](https://github.com/mohbasem25/qa-api-restassured/actions/workflows/api-tests.yml)

API test automation portfolio project covering the same target API with two
complementary, industry-standard approaches:

1. **Java + REST Assured + TestNG** — a code-based, CI-integrated automation
   framework (`restassured/`)
2. **Postman + Newman** — a collaborative, exploratory-friendly collection
   that any team member can run without writing code (`postman/`)

Target API under test: **[dummyjson.com](https://dummyjson.com)**, a genuinely
free, no-API-key-required public test API widely used for API-testing
practice. It exposes realistic, stable resources (`users`, `products`,
`auth/login`) with simulated (non-persisting) write operations, which is
exactly what a portfolio-style regression suite needs.

> **Why dummyjson.com and not reqres.in?** This project originally targeted
> reqres.in. reqres.in has since moved to a paid/managed API-key model, and
> its previously-documented "free" key now returns `401 missing_api_key` on
> every endpoint — making it unsuitable for a public, unauthenticated
> portfolio CI pipeline that anyone can clone and run without provisioning a
> secret. dummyjson.com requires no authentication at all, is stable, and is
> a widely used, purpose-built substitute for this exact use case, so the
> entire suite (Java + REST Assured, Postman/Newman, docs) was migrated to
> it wholesale.

---

## Why this project

Interviewers and hiring managers for QA/SDET roles want to see more than "I
can write a test." This project is built to demonstrate:

- **Framework design, not just test scripts** — reusable request/response
  specifications, externalised configuration, POJO models, and a clear
  package structure that mirrors what's expected in a production test
  framework.
- **Breadth of technique** — positive and negative testing, data-driven
  tests, JSON Schema contract validation, response-time SLAs, header
  assertions, and multi-step chained workflows (create → update → delete).
- **Two tools, two purposes, one mindset.** A QA engineer who only knows
  Postman can't scale a regression suite into CI. One who only knows code
  can't hand a quick smoke-test collection to a manual tester or a product
  manager. Knowing when to reach for which tool — and being fluent in both —
  is a real, practical skill this project is meant to showcase.
- **CI-mindedness** — both suites run automatically on every push/PR via
  GitHub Actions, with test reports published as build artifacts.

---

## Why two approaches?

| | REST Assured + TestNG | Postman + Newman |
|---|---|---|
| **Best for** | Regression suites, CI/CD pipelines, complex assertions, reusable frameworks | Quick smoke tests, exploratory testing, sharing with non-coders, rapid prototyping |
| **Audience** | Engineers comfortable reading/writing Java | Anyone on the team — QA, dev, PM |
| **Version control friendliness** | Excellent — plain Java diffs cleanly | Good — JSON diffs are noisier but still reviewable |
| **Extensibility** | High — full programming language, easy to add utilities/reporting/data providers | Moderate — JS in pre-request/test scripts, limited structure at scale |
| **Typical use in this project** | Full regression suite, schema validation, SLA checks, data-driven negative testing | Fast collection to hand to a teammate or run ad hoc / in CI as a lightweight smoke gate |

In real QA teams, both exist side by side: Postman collections are often the
first thing written while exploring a new API or feature, and are gradually
"graduated" into a maintained, code-based framework (like the REST Assured
suite here) once the API stabilises and needs to run reliably in CI on every
commit. This repository intentionally shows both ends of that lifecycle.

---

## Repository structure

```
qa-api-restassured/
├── restassured/                          # Java + REST Assured + TestNG framework
│   ├── pom.xml
│   ├── src/main/java/
│   │   ├── config/ApiConfig.java         # Loads config.properties, exposes typed getters
│   │   ├── specs/RequestSpecs.java       # Reusable RequestSpecification (base URI, timeouts, logging)
│   │   ├── specs/ResponseSpecs.java      # Reusable ResponseSpecification (status, content-type, SLA)
│   │   └── models/                       # POJOs: User, Product, CreateUserRequest/Response, LoginRequest/Response
│   ├── src/main/resources/config.properties
│   └── src/test/java/tests/
│       ├── BaseTest.java                 # Wires the default RequestSpecification for all test classes
│       ├── UsersGetTests.java            # GET /users, /users/{id}, /products (pagination, 200, 404)
│       ├── UsersCreateUpdateDeleteTests.java  # POST/PUT/PATCH/DELETE /users (+ create-then-delete workflow)
│       ├── AuthTests.java                # POST /auth/login (success + 400 negative cases, data-driven)
│       └── SchemaValidationTests.java    # JSON Schema contract validation for key responses
│   └── src/test/resources/
│       ├── testng.xml                    # TestNG suite definition
│       └── schemas/*.json                # JSON Schemas used by SchemaValidationTests
├── postman/
│   ├── qa-api-restassured.postman_collection.json    # Users/Auth/Products requests with pm.test assertions + chaining
│   └── qa-api-restassured.postman_environment.json   # baseUrl / chained variables
├── .github/workflows/api-tests.yml       # CI: runs both suites on push/PR
├── .gitignore
└── LICENSE
```

---

## REST Assured framework architecture

- **`ApiConfig`** centralises all environment configuration (base URI, base
  path, timeouts, response-time SLA) loaded from `config.properties`, with
  every value overridable via `-Dkey=value` on the Maven command line — no
  hard-coded values inside test code.
- **`RequestSpecs.baseSpec()`** returns a single `RequestSpecification` used
  by every test: base URI/path, JSON content type, connection/socket
  timeouts, and `RequestLoggingFilter` / `ResponseLoggingFilter` so full
  request/response payloads are printed automatically whenever an assertion
  fails (kept quiet otherwise, so CI logs stay readable). No auth header is
  needed — dummyjson.com is fully unauthenticated.
- **`ResponseSpecs`** centralises response-level expectations (status code,
  content type, response-time SLA) that are reused across tests instead of
  being repeated inline.
- **`models/`** contains plain POJOs (via Lombok `@Data`) used to
  deserialize responses (`response.as(User.class)`) and serialize request
  bodies, proving the framework isn't just doing raw string/JSON-path
  assertions but also exercising typed (de)serialization.
- **Test classes** extend `BaseTest`, which wires the shared
  `RequestSpecification` into `RestAssured.requestSpecification` once per
  class via `@BeforeClass`.
- **Data-driven testing** uses TestNG `@DataProvider` for page numbers,
  user IDs, and invalid auth payloads, avoiding copy-pasted test methods.
- **Contract testing** — `SchemaValidationTests` uses REST Assured's
  `json-schema-validator` module (`matchesJsonSchemaInClasspath`) against
  schema files under `src/test/resources/schemas/`, so structural
  regressions in the API (missing/renamed/mistyped fields) fail the build
  even if individual field assertions would still pass.

---

## Test coverage summary

| Area | Test class | Scenarios covered |
|---|---|---|
| Users — read | `UsersGetTests` | List with skip 0 & 10 with pagination metadata, per-record field shape, get existing user (5 IDs, data-driven) with typed deserialization, get non-existent user → 404 with descriptive message, list products, get non-existent product → 404, `Content-Type` header check |
| Users — write | `UsersCreateUpdateDeleteTests` | Create (simulated) user → 201 + typed response shape, create with empty body → 201, full update (PUT) → 200, partial update (PATCH) → 200, delete → 200 with `isDeleted`/`deletedOn`, end-to-end create-then-delete workflow |
| Auth | `AuthTests` | Login success (real seeded user `emilys`) → access token, login with wrong password → 400, login without password → 400, 3 data-driven invalid-payload variants |
| Contract / schema | `SchemaValidationTests` | Single user, list users, create user, and login responses each validated against a JSON Schema |
| **Total** | 4 classes | **~25 executed test cases** (including data-driver expansions), spanning positive, negative, and edge cases |

Every test also implicitly asserts the response-time SLA (`< 3000ms`,
configurable) via the shared response spec / explicit `time()` matcher.

The Postman collection mirrors the core flows above (list, get, 404, create
→ update → delete chain, login success/failure cases, products) with
`pm.test()` assertions for status code, response time, field values, and a
lightweight JSON-schema check on the list-users response, plus variable
chaining: the id returned by "Create user (simulated)" is stored in an
environment variable for narrative continuity (dummyjson.com does not
persist simulated writes, so the actual Update/Delete requests in this
collection target a known existing user id).

---

## Running the REST Assured suite

Requirements: JDK 17+, Maven 3.9+.

```bash
cd restassured
mvn test
```

This runs `src/test/resources/testng.xml`. Surefire reports are written to
`restassured/target/surefire-reports/`.

Override configuration at runtime if needed, e.g. to point at a different
environment:

```bash
mvn test -Dbase.uri=https://dummyjson.com
```

## Running the Postman collection via Newman

Requirements: Node.js 18+.

```bash
npm install -g newman
newman run postman/qa-api-restassured.postman_collection.json \
  -e postman/qa-api-restassured.postman_environment.json
```

Optional HTML report:

```bash
npm install -g newman newman-reporter-htmlextra
newman run postman/qa-api-restassured.postman_collection.json \
  -e postman/qa-api-restassured.postman_environment.json \
  --reporters cli,htmlextra --reporter-htmlextra-export newman/report.html
```

The collection can also be imported directly into the Postman desktop/web
app along with the environment file for manual/exploratory testing.

---

## Continuous Integration

`.github/workflows/api-tests.yml` runs on every push and pull request to
`main` with two independent jobs:

- **`restassured-tests`** — sets up JDK 17 and runs `mvn -f restassured/pom.xml test`, publishing Surefire reports as a build artifact.
- **`postman-tests`** — sets up Node.js, installs `newman` (+ HTML reporter), and runs the Postman collection against the environment file, publishing the HTML report as a build artifact.

Both jobs run independently so a failure in one approach doesn't block
visibility into the other.

---

## Tech stack

- Java 17, Maven
- REST Assured 5.x, `rest-assured` `json-schema-validator` module
- TestNG 7.x (`@DataProvider`, parallel suite execution)
- Jackson (POJO serialization/deserialization)
- Lombok
- Postman Collection v2.1, Newman
- GitHub Actions

---

## License

MIT — see [LICENSE](./LICENSE).
