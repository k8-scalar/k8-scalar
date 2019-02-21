## Prerequisite

We are still in the process of making the code of Scalar publicly available. 
Right now, in order to compile a Scalar experiment you first need to to add the [scalar-1.0.0.jar](../../development/scalar/)
to your Maven repository by executing the following command in the `development/scalar` directory in your local git repository of k8-scalar
```
mvn install:install-file -Dfile=scalar-1.0.0.jar -DgroupId=be.kuleuven.distrinet.scalar -DartifactId=scalar -Dversion=1.0.0 -Dpackaging=jar
```
   
JavaDoc documentation is available in the `development/scalar/doc` directory in your local git repository of k8-scalar 

## Scalar development workflow

Scalar requires the installation of the Java Development Kit (JDK) version 1.8. Installing the Java Runtime Environment (JRE) only is not enough.

All dependencies are managed via Maven
Import the project in Eclipse as a Maven project. Depending on the Eclipse version, either:
  * Import the project as a Maven project.
  * Open the project from the directory. Then right click on the project. Click on the appearing menu: -> Configure -> Convert to Maven project. 



To execute a dummy Scalar experiment on your local machine, you can start Scalar with the default `experiment.properties` and `platform.properties` 
configuration files. Start the Java class Launcher.class in Eclipse with as command-line arguments `conf/platform.properties conf/experiment.properties`.

You also compile the Scalar code at the command line with `mvn package`.  

You execute the dummy Scalar experiment as 
```
java -jar target/scalar-1.0.0.jar conf/platform.properties conf/experiment.properties
```

To include Scalar in your own Maven project, you can use the `scalar-project-pom.xml` as a template 
The source code does also include unit tests that coverage most of Scalar's functionality 

To develop a particular workload type, you have to design different subclasses of the `be.kuleuven.distrinet.scalar.core.User` and `be.kuleuven.distrinet.scalar.core.Request classes`.  
The [following paper](./heyman_preuveneers_joosen.pdf) illustrates in detail how to implemnt such User and Request classes
You can also take a look at the implementation of the Cassandra..User and Cassandra..Request classes [here](../../development/scalar/src/be/kuleuven/distrinet/scalar/
)
