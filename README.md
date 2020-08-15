# CLI

[![Build Status](https://travis-ci.org/openjax/cli.svg?branch=master)](https://travis-ci.org/openjax/cli)
[![Coverage Status](https://coveralls.io/repos/github/openjax/cli/badge.svg)](https://coveralls.io/github/openjax/cli)
[![Javadocs](https://www.javadoc.io/badge/org.openjax/cli.svg)](https://www.javadoc.io/doc/org.openjax/cli)
[![Released Version](https://img.shields.io/maven-central/v/org.openjax/cli.svg)](https://mvnrepository.com/artifact/org.openjax/cli)
![Snapshot Version](https://img.shields.io/nexus/s/org.openjax/cli?label=maven-snapshot&server=https%3A%2F%2Foss.sonatype.org)

## Introduction

OpenJAX CLI is a light wrapper around the [Apache Commons CLI][apache-commons-cli] library, which provides a simple API to specify and process command line arguments.

OpenJAX CLI allows a developer the full range of variation of the command line arguments pattern, and removes the unnecessary boilerplate code present in other solutions. **cli** uses the JAXB framework to create a **cli** specification based on a [XSD specification][cli-schema], which can be used to create a custom set of CLI options and arguments for any application.

### Validating and Fail-Fast

OpenJAX CLI is based on a [XML Schema][cli-schema] that is used to specify the format of XML documents that describe the command line options and arguments accepted by an application. The XML Schema is designed to use the full power of XML Validation to allow a developer to quickly determine errors in his draft. Once a `cli.xml` passes the validation checks, it thereafter provides a clear and simple API to access the options and arguments in the code.

## Getting Started

### Prerequisites

* [Java 8][jdk8-download] - The minimum required JDK version.
* [Maven][maven] - The dependency management system.

### Example

1. Create a `cli.xml` in `src/main/resources/`.

   ```xml
   <cli
     xmlns="http://www.openjax.org/cli-1.1.xsd"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.openjax.org/cli-1.1.xsd http://www.openjax.org/cli.xsd">
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

1. Add `org.openjax:cli` dependency to the POM.

   ```xml
   <dependency>
     <groupId>org.openjax</groupId>
     <artifactId>cli</artifactId>
     <version>1.1.9</version>
   </dependency>
   ```

1. Before any other code in the `main()` method in `App.java`, add the following line and let your IDE resolve the missing imports.

   ```java
   Options options = Options.parse(ClassLoader.getSystemClassLoader().getResource("cli.xml").getURL(), args);
   ```

   Options can now be accessed as such: `options.getOption("config")`.

   Arguments can now be accesses ad such: `options.getArguments()`.

1. When you use CLI, you get a `--help` option automatically provided. After compiling your application, run the app with `App --help`, and you should see this:

   ```bash
   usage:
   [options] <FILE> <FILE2> <FILE3> [FILE4] [FILE5] [...]
    -C,--config <CONFIG_FILE>    config file to use instead of default
                                 default: <config.xml>
       --help                    Print help and usage.
       --silent                  silent mode
    -V                           verbose mode
   ```

## Contributing

Pull requests are welcome. For major changes, please [open an issue](../../issues) first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

[apache-commons-cli]: https://commons.apache.org/proper/commons-cli/
[cli-schema]: /src/main/resources/cli.xsd
[jdk8-download]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[maven-archetype-quickstart]: http://maven.apache.org/archetypes/maven-archetype-quickstart/
[maven]: https://maven.apache.org/