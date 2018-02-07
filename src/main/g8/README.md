**Summary**
--
Project structure:    
1) `<root>/src/main/resources/application.conf` - configuration file;  
2) `<root>/src/main/resources/logback.xml` - logback configuration;

#### Development requirements
1) Scala $scala_version$;  
2) SBT $sbt_version$;  

# Configuration

#### How to install sbt
1) [MacOS](http://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Mac.html)  
2) [Windows](http://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Windows.html)   
3) [Linux](http://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html)   

#### How to run tests
In a `<root>` project directory write in a console  
```scala
sbt test
```

#### How to calculate coverage
```sbtshell
sbt clean coverage test it:test coverageReport
```

Coverage reports will be in `target/scoverage-report`. There are HTML and XML reports. The XML is useful if you need to programatically use the results, or if you're writing a tool.
 
#### How to configure an application
All settings are stored in the `application.conf`.  
1) `application.http.host` - http host;  
2) `application.http.port` - http port;  
6) `LOG_LEVEL` - specify an environment variable to change a log level.  
By default, application is listening on port 9001.  
 
#### How to build a standalone application
In a `<root>` project folder write in a console  
```sbtshell
sbt universal:packageBin
```

The output file will be located at this place:
```
<root>/target/universal/$name_normalized$.zip
```

#### How to run an application
Unzip an `$name_normalized$.zip` archive in any directory (`<root>/dist`, for example).    
Execute in a `<root>/dist/$name_normalized$` folder:    
```sbtshell
sh bin/$name_normalized$
```

#### How to create a dev docker image
SBT will publish an image locally using a name based on a git hash.  
Execute in a `<root>` project folder:  
```sbtshell
sbt dev:docker
```

#### How to run a docker container
Check configuration properties in the `docker-compose.yml` file.  
Execute in a `<root>/docker` project folder: 
```
docker-compose up
```

#### How to run release a version
Execute in a `<root>` project folder:  
```sbtshell
sbt "release with-defaults"
```

#### How to create self-signed SSL certificates for Nginx
Execute in a `<root>/docker/nginx` project folder and follow commands:  
```sbtshell
sh gen-cert.sh
```

Detailed instruction is [here](https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-16-04).
