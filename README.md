<img src="http://safris.org/logo.png" align="right" />
# CPI [![CohesionFirst](http://safris.org/cf2.svg)](https://cohesionfirst.com/)
> Command Line Interface

## Introduction

CLI is a light wrapper around the [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) library that provides a simple API to specify and process command line arguments.

## Why CLI?

### CohesionFirst™

Developed with the CohesionFirst™ approach, CLI is an easy-to-use and simple solution that separates itself from the rest with the strength of its cohesion. Made possible by the rigorous conformance to design patterns and best practices in every line of its implementation, CLI is a complete solution for the command line arguments facet of an application.

### Complete Solution

CLI allows a developer the full range of variation of the command line arguments pattern, and removes the unnecessary boilerplate code present in other solutions. CLI uses the XSB framework for [XML Schema Binding](https://github.com/SevaSafris/xsb/) to create a CLI specification based on a [XSD specification](https://github.com/SevaSafris/java/blob/master/commons/cli/src/main/resources/cli.xsd).

### Validating and Fail-Fast

CLI is based on a [XML Schema](https://github.com/SevaSafris/java/blob/master/commons/cli/src/main/resources/cli.xsd) used to specify the formal of XML documents that describe the command line arguments accepted by an application. The XML Schema is designed to use the full power of XML Validation to allow a developer to qiuckly determine errors in his draft. Once a `cli.xml` passes the validation checks, it thereafter provides a clear and simple API to access the options and arguments in the code.

## Getting Started

### Prerequisites

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) - The minimum required JDK version.
* [Maven](https://maven.apache.org/) - The dependency management system used to install XDL.

### Example

1. In your preferred development directory, create a [`maven-archetype-quickstart`](http://maven.apache.org/archetypes/maven-archetype-quickstart/) project.

  ```tcsh
  mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
  ```

2. Add the `mvn.repo.safris.org` Maven repositories to the POM.

  ```xml
  <repositories>
    <repository>
      <id>mvn.repo.safris.org</id>
      <url>http://mvn.repo.safris.org/m2</url>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>mvn.repo.safris.org</id>
      <url>http://mvn.repo.safris.org/m2</url>
    </pluginRepository>
  </pluginRepositories>
  ```

3. Create a `cli.xml` in `src/main/resources/`.

  ```xml
  <cli
    xmlns="http://commons.safris.org/cli.xsd"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://commons.safris.org/cli.xsd http://commons.safris.org/cli.xsd">
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

4. Add `org.safris.commons`:`cli` dependency to the POM.

  ```xml
  <dependency>
    <groupId>org.safris.commons</groupId>
    <artifactId>cli</artifactId>
    <version>2.1.5</version>
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

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
