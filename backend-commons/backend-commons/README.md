# Eucalyptus Labs Backend Commons

The common classes shared among all microservices.

### Build

* Build: `mvnw clean install`
* Build without tests: `mvnw clean install -DskipTests`

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
