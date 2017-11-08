<img src="https://www.cohesionfirst.org/logo.png" align="right">

## lib4j-cli<br>![java-commons][java-commons] <a href="https://www.cohesionfirst.org/"><img src="https://img.shields.io/badge/CohesionFirst%E2%84%A2--blue.svg"></a>
> Commons Command Line Interface

### Introduction

**lib4j-cli** is a light wrapper around the [Apache Commons CLI][apache-commons-cli] library, which provides a simple API to specify and process command line arguments.

### Why **lib4j-cli**?

#### CohesionFirst™

Developed with the CohesionFirst™ approach, **lib4j-cli** is an easy-to-use and simple solution that separates itself from the rest with the strength of its cohesion and ease of usability. Made possible by the rigorous conformance to best practices in every line of its implementation, **lib4j-cli** considers the needs of the developer as primary, and offers a complete solution for the command line arguments facet of an application.

#### Complete Solution

**lib4j-cli** allows a developer the full range of variation of the command line arguments pattern, and removes the unnecessary boilerplate code present in other solutions. **lib4j-cli** uses the JAXB framework to create a **lib4j-cli** specification based on a [XSD specification][cli-schema], which can be used to create a custom set of CLI options and arguments for any application.

#### Validating and Fail-Fast

**lib4j-cli** is based on a [XML Schema][cli-schema] that is used to specify the format of XML documents that describe the command line options and arguments accepted by an application. The XML Schema is designed to use the full power of XML Validation to allow a developer to quickly determine errors in his draft. Once a `cli.xml` passes the validation checks, it thereafter provides a clear and simple API to access the options and arguments in the code.

### Getting Started

#### Prerequisites

* [Java 8][jdk8-download] - The minimum required JDK version.
* [Maven][maven] - The dependency management system.

#### Example (Quick-&-Easy)

1. In your preferred development directory, create a [`cli-maven-archetype`][cli-maven-archetype] project.

    ```tcsh
    mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app \
    -DarchetypeGroupId=org.lib4j.maven.archetype -DarchetypeArtifactId=cli-maven-archetype \
    -DarchetypeCatalog=http://mvn.repo.lib4j.org -DinteractiveMode=false
    ```

#### Example (Hands-on)

1. In your preferred development directory, create a [`maven-archetype-quickstart`][maven-archetype-quickstart] project.

    ```tcsh
    mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app \
    -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
    ```

2. Add the `mvn.repo.lib4j.org` Maven repositories to the POM.

    ```xml
    <repositories>
      <repository>
        <id>mvn.repo.lib4j.org</id>
        <url>http://mvn.repo.lib4j.org/m2</url>
      </repository>
    </repositories>
    <pluginRepositories>
      <pluginRepository>
        <id>mvn.repo.lib4j.org</id>
        <url>http://mvn.repo.lib4j.org/m2</url>
      </pluginRepository>
    </pluginRepositories>
    ```

3. Create a `cli.xml` in `src/main/resources/`.

    ```xml
    <cli
      xmlns="http://commons.lib4j.org/cli.xsd"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://lib4j.org/cli.xsd http://lib4j.org/cli.xsd">
      <option>
        <name long="config" short="C"/>
        <argument label="CONFIG_FILE" use="required" default="config.xml"/>
        <description>config file to use instead of default</description>
      </option>
      <option>
        <name long="silent"/>
        <description>silent mode</description>
      </option>
      <option>
        <name short="V"/>
        <description>verbose mode</description>
      </option>
      <arguments label="FILE" minOccurs="3" maxOccurs="unbounded"/>
    </cli>
    ```
  
    This `cli.xml` describes 3 options and 1 argument. The 3 options are "config", which has its own required argument of CONFIG_FILE, "silent", and "verbose." Lastly, the argument FILE is required with a cardinality of 3 or more.

4. Add `org.lib4j:lib4j-cli` dependency to the POM.

    ```xml
    <dependency>
      <groupId>org.lib4j</groupId>
      <artifactId>lib4j-cli</artifactId>
      <version>2.1.6-SNAPSHOT</version>
    </dependency>
    ```

5. Before any other code in the `main()` method in `App.java`, add the following line and let your IDE resolve the missing imports.

    ```java
    final Options options = Options.parse(Resources.getResource("cli.xml").getURL(), App.class, args);
    ```

    Options can now be accessed as such: `options.getOption("config")`.

    Arguments can now be accesses ad such: `options.getArguments()`.

6. When you use CLI, you get a `--help` option automatically provided. After compiling your application, run the app with `App --help`, and you should see this:

    ```tcsh
    usage:
    [options] <FILE> <FILE2> <FILE3> [FILE4] [FILE5] [...]
     -C,--config <CONFIG_FILE>    config file to use instead of default
                                  default: <config.xml>
        --help                    Print help and usage.
        --silent                  silent mode
     -V                           verbose mode
    ```

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

[apache-commons-cli]: https://commons.apache.org/proper/commons-cli/
[cli-maven-archetype]: https://github.com/lib4j/cli-maven-archetype
[cli-schema]: https://github.com/lib4j/lib4j-cli/blob/master/src/main/resources/cli.xsd
[java-commons]: https://img.shields.io/badge/java-lib4j-orange.svg
[jdk8-download]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[maven-archetype-quickstart]: http://maven.apache.org/archetypes/maven-archetype-quickstart/
[maven]: https://maven.apache.org/