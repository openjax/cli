# OpenJAX Support CLI

> Command Line Interface

[![Build Status](https://travis-ci.org/openjax/support-cli.png)](https://travis-ci.org/openjax/support-cli)
[![Coverage Status](https://coveralls.io/repos/github/openjax/support-cli/badge.svg)](https://coveralls.io/github/openjax/support-cli)

### Introduction

**cli** is a light wrapper around the [Apache Commons CLI][apache-commons-cli] library, which provides a simple API to specify and process command line arguments.

### Why **cli**?

#### CohesionFirst

Developed with the CohesionFirst approach, **cli** is an easy-to-use and simple solution that separates itself from the rest with the strength of its cohesion and ease of usability. Made possible by the rigorous conformance to best practices in every line of its implementation, **cli** considers the needs of the developer as primary, and offers a complete solution for the command line arguments facet of an application.

#### Complete Solution

**cli** allows a developer the full range of variation of the command line arguments pattern, and removes the unnecessary boilerplate code present in other solutions. **cli** uses the JAXB framework to create a **cli** specification based on a [XSD specification][cli-schema], which can be used to create a custom set of CLI options and arguments for any application.

#### Validating and Fail-Fast

**cli** is based on a [XML Schema][cli-schema] that is used to specify the format of XML documents that describe the command line options and arguments accepted by an application. The XML Schema is designed to use the full power of XML Validation to allow a developer to quickly determine errors in his draft. Once a `cli.xml` passes the validation checks, it thereafter provides a clear and simple API to access the options and arguments in the code.

### Getting Started

#### Prerequisites

* [Java 8][jdk8-download] - The minimum required JDK version.
* [Maven][maven] - The dependency management system.

#### Example (Quick-&-Easy)

1. In your preferred development directory, create a [`cli-maven-archetype`][cli-maven-archetype] project.

    ```tcsh
    mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app \
    -DarchetypeGroupId=org.openjax.support -DarchetypeArtifactId=cli-maven-archetype \
    -DarchetypeCatalog=http://mvn.repo.openjax.org -DinteractiveMode=false
    ```

#### Example (Hands-on)

1. In your preferred development directory, create a [`maven-archetype-quickstart`][maven-archetype-quickstart] project.

    ```tcsh
    mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app \
    -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
    ```

2. Add the `mvn.repo.openjax.org` Maven repositories to the POM.

    ```xml
    <repositories>
      <repository>
        <id>mvn.repo.openjax.org</id>
        <url>http://mvn.repo.openjax.org/m2</url>
      </repository>
    </repositories>
    <pluginRepositories>
      <pluginRepository>
        <id>mvn.repo.openjax.org</id>
        <url>http://mvn.repo.openjax.org/m2</url>
      </pluginRepository>
    </pluginRepositories>
    ```

3. Create a `cli.xml` in `src/main/resources/`.

    ```xml
    <cli
      xmlns="http://support.openjax.org/cli-1.1.7.xsd"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://support.openjax.org/cli-1.1.7.xsd http://support.openjax.org/cli.xsd">
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

4. Add `org.openjax.support:cli` dependency to the POM.

    ```xml
    <dependency>
      <groupId>org.openjax.support</groupId>
      <artifactId>support-cli</artifactId>
      <version>1.1.7-SNAPSHOT</version>
    </dependency>
    ```

5. Before any other code in the `main()` method in `App.java`, add the following line and let your IDE resolve the missing imports.

    ```java
    Options options = Options.parse(Thread.currentThread().getContextClassLoader().getResource("cli.xml").getURL(), App.class, args);
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

### JavaDocs

JavaDocs are available [here](https://support.openjax.org/cli/apidocs/).

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

[apache-commons-cli]: https://commons.apache.org/proper/commons-cli/
[cli-maven-archetype]: /../../../../openjax/cli-maven-archetype
[cli-schema]: /src/main/resources/cli.xsd
[jdk8-download]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[maven-archetype-quickstart]: http://maven.apache.org/archetypes/maven-archetype-quickstart/
[maven]: https://maven.apache.org/