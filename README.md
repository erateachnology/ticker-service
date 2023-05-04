# Ticker Service

Ticker backend service serves the main endpoints(V1 and V2) with following information:

- Will call a REST endpoints from different providers through a schedular and get the data and push and update in a cache. Why we went for this solution is these endpoints associate with a coset. Once you hit to this endpoint it will cost you. To avoid such overhead cost we update data for every 5 mins and when user requests data will be taken and returning from the cache.

### Initial build

Set up Maven Wrapper:

```
mvn -N io.takari:maven:wrapper
```

Checkout the submodule:

```
git submodule init
git submodule update
```

Add submodule's files `backend-commons/pom.xml`,
`backend-commons/backend-commons/pom.xml` as Maven projects in IntelliJ Idea. You can do it by right-clicking on
the file and selecting `Add as Maven project`.

Build the submodule project:

```
./mvnw -f backend-commons/pom.xml clean install
./mvnw -f backend-commons/backend-commons/pom.xml clean install
```

Build the main project:

```
./mvnw -f pom.xml clean install -DskipTests=true
```

### Build

* Build: `./mvnw clean install`
* Build without tests: `./mvnw clean install -DskipTests`

### Development Run

* Run application: `./mvnw -Pspring-doc spring-boot:run -D"spring-boot.run.profiles"=local`
  (builds with `spring-doc` Maven profile, with Spring
  profiles `application-develop.properties and application-local-overrides.properties`)
* Run tests: `./mvnw test`

**Note:** Sometimes application may fail to run because it can not find or create directory for logging mentioned inside
the logback.xml with property named `LOGS_DIRECTORY`. To solve this, either create the directory mentioned in the
logback.xml or point `LOGS_DIRECTORY`
to an already existing directory.

### Testing

There are three categories of testing in the project:

* integration tests (invoking the real test API of data providers) `src/integration-test

```
./mvnw verify -Pintegration-test
```

* functional tests (unit tests starting Spring context and invoking mocked endpoints) `src/functional-test`

```
./mvnw test
```

* unit tests `src/test`.

```
./mvnw test
```

### Production Run

* Run application: `./mvnw spring-boot:run -Dspring-boot.run.profiles=production`

### Docs

When running with `spring-doc` Maven profile the app exposes Swagger UI
under `http://localhost:9061/swagger-ui/index.html`. The `documentation` directory contains Postman collection covering
endpoints of the service.

## Deployment

The Ticker Service is currently deployed on `t` (internal IP ``). The CD setup has been done
accordingly to the documentation:
https://gitlab.com/EucalyputsLabs/eucalyptuslabs-documentation/-/blob/master/infrastructure/auto-deploy-from-gitlab.md

