## Akka HTTP template

You can use [Giter8](https://github.com/foundweekends/giter8) to create your own project from the template.

Prerequisites:
- JDK 8
- Giter8 0.10.0

Open a console and run the following command to apply this template:
 ```
g8 https://github.com/iRevive/akka-http-template.g8
 ```

## Template configuration

This template will prompt for the following parameters. Press `Enter` if the default values suit you:
- `name`: Becomes the name of the project.
- `organization`: Specifies the organization for this project.
- `scala_version`: Specifies the Scala version for this project.
- `akka_http_version`: Specifies which version of Akka HTTP should be used for this project.
- `akka_version`: Specifies which version of Akka Actor/Stream should be used for this project.
- `monix_version`: Specifies which version of Monix should be used for this project.
- `cats_version`: Specifies which version of Cats should be used for this project.
- `circe_version`: Specifies which version of Circe should be used for this project.
- `circe_config_version`: Specifies which version of Circe-Config should be used for this project.
- `refined_version`: Specifies which version of Refined should be used for this project.
- `simulacrum_version`: Specifies which version of Simulacrum should be used for this project.
- `magnorlia_version`: Specifies which version of Magnolia should be used for this project.
- `sourcecode_version`: Specifies which version of Sourcecode should be used for this project.
- `mongo_scala_driver_version`: Specifies which version of Mongo Scala Driver should be used for this project.
- `netty_version`: Specifies which version of Netty should be used for this project.
- `logback_version`: Specifies which version of Logback should be used for this project.
- `scala_logging_version`: Specifies which version of Scala Logging should be used for this project.
- `scalatest_version`: Specifies which version of Scalatest should be used for this project.
- `cats_scalatest_version`: Specifies which version of Cats-scalatest should be used for this project.
- `useNginx`: If true, generates an nginx config for docker.
- `useMongo`: If true, generates a config for Mongo Scala Driver and a config for docker.

The template comes with the following sources:

* `GeneralApi.scala` -- the class which handles requests.
* `PersistenceModule.scala` -- the class which has a MongoDB initialization logic.
* `Server.scala` -- the main class which starts up the HTTP server.
* `GenerlaApiSpec.scala` -- the class which tests routes.
* `PersistenceSpec.scala` -- the class which has an integration test for a persistence module.
* `docker-compose.yml` -- docker compose configuration. 
* `README.md` - the documentation with explanation of all project functions.

Once inside the project folder use the following command to run the code:
```
sbt clean test it:test run
```

## SBT plugins

#### [sbt-release](https://github.com/sbt/sbt-release)
The plugin configured without `publishArtifact` step. By default, it will publish a docker image locally.  

#### [sbt-native-packager](https://github.com/sbt/sbt-native-packager)
Almost default configuration.

#### [sbt-scoverage](https://github.com/scoverage/sbt-scoverage)
Default configuration without changes. Coverage disabled by default.

#### [sbt-scalafmt](https://github.com/scalameta/sbt-scalafmt)
Default configuration without changes. Coverage disabled by default.
 
## Integration tests

This template will generate a docker-based environment for integration tests.  
On the start of integration tests sbt will start MongoDB as a docker container, after tests it will be destroyed.

## Docker-compose configuration

This template will generate a docker-compose file with configuration for app, MongoDB and Nginx.

**Note:** create a self-singed certificates to use Nginx from docker. Execute in a `<root>/docker/nginx` project folder and follow commands:  
```
sh gen-cert.sh
```

Detailed instruction is [here](https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-16-04).



