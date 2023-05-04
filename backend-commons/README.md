# Eucalyptus Labs Microservice Parent

The parent pom.xml from which microservices should inherit.

### Build

Build the project:
```
./mvnw -f backend-commons/pom.xml clean install
./mvnw -f backend-commons/backend-commons/pom.xml clean install
```

### Building on Jenkins

Artifacts built with version having -SNAPSHOT suffix will be replaced
in the local repository on Jenkins. It is important to increase the version
when doing some major changes as newly built artifact would
overwrite the previous SNAPSHOT version and possibly break the builds
of other projects on Jenkins.

If you want your microservice to get stable version of the library
use non snapshot version.

On Jenkins the Commons Project is built as a separate job. When the job
is successful all dependent projects are being built.

### Building on GitLab

When building on GitLab each build is processed in the isolated
container, so the local Maven repository is not a concern.
The only effective parameter for the build is a commit id
of a submodule. Updating the Git submodule will cause the updated
library to be included into microservice build. The version of
the library in submodule should match the dependency version
in microservice pom.xml.

### Development Run

* Run application: `mvnw -Pswagger-ui spring-boot:run -Dspring-boot.run.profiles=local`
  (builds with `spring-doc` Maven profile, with Spring
  profiles `application-develop.properties and application-local-overrides.properties`)
* Run tests: `mvnw test`

**Note:** Sometimes application may fail to run because it can not find or create directory for logging mentioned inside
the logback.xml with property named `LOGS_DIRECTORY`. To solve this, either create the directory mentioned in the
logback.xml or point `LOGS_DIRECTORY`
to an already existing directory.

### Testing

There are three categories of testing in the project:

* integration tests (invoking the real test API of data providers) `src/integration-test`
* functional tests (unit tests starting Spring context and invoking mocked endpoints) `src/functional-test`
* unit tests `src/test`.

### Production Run

* Run application: `mvnw spring-boot:run -Dspring-boot.run.profiles=production`

### Docs

When running with `spring-doc` Maven profile the app exposes Swagger UI
under `http://localhost:9021/spring-doc/index.html`. The `documentation` directory contains Postman collection covering
endpoints of the service.

## Deployment

The CD setup has been done
accordingly to the documentation:
https://gitlab.com/Eucalyptus Labs/eucalyptuslabs-documentation/-/blob/master/infrastructure/auto-deploy-from-gitlab.md
