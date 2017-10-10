Running Arquillian tests for OntoBrowser
---
### Running
Test names are postfixed with ArqTestIT and run like normal JUnit test, but are not included in standard Maven build.

### Configuration

#### Maven
Arquillian test are configured in `development` Maven profile.

#### Datasource
Test run on an independent test schema, with initial data only.
The test data source is called `ontobrowserTest` and it is configured in `persistence-test.xml` file.

#### Widlfly
Arqullian enriches deployment `WAR` file with compiled tests and run those test on WildFly remotely.
WildFly __must not have Ontobrowser deployed__ when running tests.
Path to WildFly is configured in `src/test/resources/arquillian.xml` as `jbossHome` property.

#### Debugging
As test are run remotely, also the debug need to be configured as remote.
Debug listener is configured on port `8787` in `pom.xml` in WildFly plugin configuration.

#### JavaMelody performance monitoring
In the `develpoment` profile OntoBrowser is enriched with JavaMelody performance monitoring which:
* collects most common errors,
* shows the slow sql queries,
* shows the stacktraces of running web request.

Default access url: [http://localhost:8080/ontobrowser/monitoring](http://localhost:8080/ontobrowser/monitoring)

It has no significant impact on the monitored application.

