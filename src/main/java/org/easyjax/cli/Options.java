/* Copyright (c) 2008 EasyJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.easyjax.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.FixedHelpFormatter;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.easyjax.cli_2_1_7.Cli;
import org.easyjax.cli_2_1_7.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class Options {
  private static final Logger logger = LoggerFactory.getLogger(Options.class);
  private static Schema schema;

  private static String formatArgumentName(final String label, final int maxOccurs, final char valueSeparator) {
    if (maxOccurs == 1)
      return label;

    final StringBuilder buffer = new StringBuilder(label);
    buffer.append(1).append(valueSeparator);

    if (maxOccurs == 2)
      return buffer.append(label).append(2).toString();

    if (maxOccurs == Integer.MAX_VALUE)
      return buffer.append(label).append(2).append("...").toString();

    return buffer.append("...").append(valueSeparator).append(label).append(maxOccurs).toString();
  }

  private static void printHelp(final org.apache.commons.cli.Options apacheOptions, final Cli.Arguments cliArguments, final PrintStream ps) {
    final HelpFormatter formatter = new FixedHelpFormatter();
    final PrintWriter pw = new PrintWriter(ps);
    final StringBuilder args = new StringBuilder(apacheOptions.getOptions().size() > 0 ? " [options]" : "");
    if (cliArguments != null) {
      for (short i = 1; i <= cliArguments.getMinOccurs(); i++)
        args.append(" <").append(cliArguments.getLabel()).append(i != 1 ? i : "").append('>');

      final boolean maxUnbounded = "unbounded".equals(cliArguments.getMaxOccurs());
      final int argsMax = maxUnbounded ? 2 + cliArguments.getMinOccurs() : Short.parseShort(cliArguments.getMaxOccurs());
      for (int i = cliArguments.getMinOccurs() + 1; i <= argsMax; i++)
        args.append(" [").append(cliArguments.getLabel()).append(i != 1 ? i : "").append(']');

      if (maxUnbounded)
        args.append(" [...]");
    }

    formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, " ", args.substring(1), apacheOptions, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, false);
    pw.flush();
  }

  private static void trapPrintHelp(final org.apache.commons.cli.Options apacheOptions, final Cli.Arguments cliArguments, final String message, final PrintStream ps) {
    if (message != null)
      ps.println(message);

    printHelp(apacheOptions, cliArguments, ps);
    System.exit(1);
  }

  public static Options parse(final File cliFile, final Class<?> mainClass, final String[] args) {
    try {
      return parse(cliFile.toURI().toURL(), mainClass, args);
    }
    catch (final MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Options parse(final URL cliURL, final Class<?> mainClass, final String[] args) {
    try {
      final Unmarshaller unmarshaller = JAXBContext.newInstance(Cli.class).createUnmarshaller();
      unmarshaller.setSchema(Options.schema == null ? Options.schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(Thread.currentThread().getContextClassLoader().getResource("cli.xsd")) : Options.schema);

      try (final InputStream in = cliURL.openStream()) {
        final JAXBElement<Cli> element = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(in), Cli.class);
        return parse(element.getValue(), mainClass, args);
      }
    }
    catch (final FactoryConfigurationError e) {
      throw new UnsupportedOperationException(e);
    }
    catch (final IOException e) {
      throw new RuntimeException(e);
    }
    catch (final JAXBException | SAXException | XMLStreamException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Options parse(final Cli binding, final Class<?> mainClass, final String[] args) {
    final Set<String> requiredNames = new HashSet<>();
    final Map<String,String> nameToAltName = new HashMap<>();
    final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
    apacheOptions.addOption(null, "help", false, "Print help and usage.");
    short argumentsMinOccurs = 0;
    short argumentsMaxOccurs = 0;
    final Cli.Arguments cliArguments;
    if (binding != null) {
      cliArguments = binding.getArguments();
      if (cliArguments != null) {
        argumentsMinOccurs = cliArguments.getMinOccurs();
        argumentsMaxOccurs = "unbounded".equals(cliArguments.getMaxOccurs()) ? Short.MAX_VALUE : Short.parseShort(cliArguments.getMaxOccurs());
        if (argumentsMaxOccurs < argumentsMinOccurs) {
          logger.error("minOccurs > maxOccurs on <arguments> element");
          System.exit(1);
        }
      }

      if (binding.getOption() != null) {
        for (final Cli.Option option : binding.getOption()) {
          final Cli.Option.Name optionName = option.getName();
          final String longName = optionName.getLong() == null ? null : optionName.getLong();
          final String shortName = optionName.getShort() == null ? null : optionName.getShort();
          final String name = longName != null ? longName : shortName;
          if (longName == null && shortName == null) {
            logger.error("both [long] and [short] option names are null in cli spec");
            System.exit(1);
          }

          nameToAltName.put(name, shortName != null ? shortName : longName);
          OptionBuilder.withLongOpt(name == longName ? longName : null);

          // Record which options are required
          if (option.getArgument() != null) {
            final Cli.Option.Argument argument = option.getArgument();
            final boolean isRequired = Use.REQUIRED == argument.getUse();
            if (isRequired) {
              OptionBuilder.isRequired();
              requiredNames.add(longName);
            }

            final int maxOccurs = argument.getMaxOccurs() == null ? 1 : "unbounded".equals(argument.getMaxOccurs()) ? Integer.MAX_VALUE : Integer.parseInt(argument.getMaxOccurs());
            if (maxOccurs == 1) {
              if (isRequired)
                OptionBuilder.hasArgs(1);
              else
                OptionBuilder.hasOptionalArgs(1);
            }
            else if (maxOccurs == Integer.MAX_VALUE) {
              if (isRequired)
                OptionBuilder.hasArgs();
              else
                OptionBuilder.hasOptionalArgs();
            }
            else {
              if (isRequired)
                OptionBuilder.hasArgs(maxOccurs);
              else
                OptionBuilder.hasOptionalArgs(maxOccurs);
            }

            final char valueSeparator = argument.getValueSeparator() != null ? argument.getValueSeparator().charAt(0) : ' ';
            OptionBuilder.withArgName(formatArgumentName(argument.getLabel(), maxOccurs, valueSeparator));
            OptionBuilder.withValueSeparator(valueSeparator);
            if (option.getDescription() == null) {
              logger.error("missing <description> for " + name + " option");
              System.exit(1);
            }

            final StringBuilder description = new StringBuilder(option.getDescription());
            if (option.getArgument().getDefault() != null)
              description.append("\nDefault: ").append(option.getArgument().getDefault());

            OptionBuilder.withDescription(description.toString());
          }

          apacheOptions.addOption(OptionBuilder.create(shortName));
        }
      }
    }
    else {
      cliArguments = null;
    }

    final Map<String,Option> optionsMap = new HashMap<>();
    final Set<String> specifiedLongNames;
    CommandLine commandLine = null;
    if (args != null && args.length != 0) {
      specifiedLongNames = new HashSet<>();
      final CommandLineParser parser = new PosixParser();
      do {
        try {
          commandLine = parser.parse(apacheOptions, args);
        }
        catch (final UnrecognizedOptionException e) {
          if (e.getMessage().startsWith("Unrecognized option: ")) {
            final String unrecognizedOption = e.getMessage().substring(21);
            logger.error("Unrecognized option: " + unrecognizedOption);
            for (int i = 0; i < args.length; i++)
              if (args[i].equals(unrecognizedOption))
                args[i] = "--help";
          }
          else {
            throw new IllegalArgumentException(e);
          }
        }
        catch (final org.apache.commons.cli.ParseException e) {
          Options.trapPrintHelp(apacheOptions, cliArguments, null, System.err);
        }
      }
      while (commandLine == null);
    }
    else {
      specifiedLongNames = null;
    }

    final Collection<String> arguments = commandLine != null ? commandLine.getArgList() : null;
    if (arguments != null && arguments.size() > 0) {
      if (argumentsMaxOccurs < arguments.size() || arguments.size() < argumentsMinOccurs) {
        Options.trapPrintHelp(apacheOptions, cliArguments, null, System.err);
      }
    }
    else if (argumentsMinOccurs > 0) {
      Options.trapPrintHelp(apacheOptions, cliArguments, null, System.err);
    }

    if (commandLine != null) {
      for (final org.apache.commons.cli.Option option : commandLine.getOptions()) {
        specifiedLongNames.add(option.getLongOpt());
        if ("help".equals(option.getLongOpt()))
          Options.trapPrintHelp(apacheOptions, cliArguments, null, System.out);

        final String optionName = option.getLongOpt() != null ? option.getLongOpt() : option.getOpt();
        optionsMap.put(optionName, option.getValue() != null ? new Option(optionName, option.getValueSeparator(), option.getValues()) : new Option(optionName, option.getValueSeparator(), "true"));
      }
    }

    // See if some arguments are missing
    if (requiredNames.size() != 0) {
      if (specifiedLongNames != null)
        requiredNames.removeAll(specifiedLongNames);

      if (requiredNames.size() != 0) {
        final StringBuilder builder = new StringBuilder();
        for (final String longName : requiredNames) {
          final String shortName = nameToAltName.get(longName);
          if (shortName.equals(longName))
            builder.append("\nMissing argument: -").append(shortName);
          else
            builder.append("\nMissing argument: -").append(shortName).append(",--").append(longName);
        }

        Options.trapPrintHelp(apacheOptions, cliArguments, builder.substring(1), System.out);
      }
    }

    // Include default values for options that are not specified
    if (binding.getOption() != null) {
      for (final Cli.Option option : binding.getOption()) {
        if (option.getArgument() != null && option.getArgument().getDefault() != null) {
          final String optionName = option.getName().getLong() != null ? option.getName().getLong() : option.getName().getShort();
          if (!optionsMap.containsKey(optionName)) {
            final String valueSeparator = option.getArgument().getValueSeparator();
            final String defaultValue = option.getArgument().getDefault();
            optionsMap.put(optionName, valueSeparator != null ? new Option(optionName, valueSeparator.charAt(0), defaultValue) : new Option(optionName, defaultValue));
          }
        }
      }
    }

    // Check pattern for specified and default options
    if (binding.getOption() != null) {
      final StringBuilder builder = new StringBuilder();
      for (final Cli.Option option : binding.getOption()) {
        if (option.getArgument() != null && option.getArgument().getPattern() != null) {
          final String optionName = option.getName().getLong() != null ? option.getName().getLong() : option.getName().getShort();
          final Option opt = optionsMap.get(optionName);
          if (opt != null) {
            for (final String value : opt.getValues()) {
              if (!value.matches(option.getArgument().getPattern())) {
                if (option.getName().getLong() == null || option.getName().getShort() == null)
                  builder.append("\nIncorrect argument form: -").append(optionName);
                else
                  builder.append("\nIncorrect argument form: -").append(option.getName().getShort()).append(",--").append(option.getName().getLong());

                builder.append(' ').append(value).append("\n  Required: ").append(option.getArgument().getPattern());
              }
            }
          }
        }
      }

      if (builder.length() > 0)
        Options.trapPrintHelp(apacheOptions, cliArguments, builder.substring(1), System.out);
    }

    return new Options(mainClass, args, optionsMap.values(), arguments == null || arguments.size() == 0 ? null : arguments.toArray(new String[arguments.size()]));
  }

  private final Map<String,Option> optionNameToOption = new HashMap<>();
  private final Class<?> mainClass;
  private final String[] args;
  private final Collection<Option> options;
  private final String[] arguments;

  private Options(final Class<?> mainClass, final String[] args, final Collection<Option> options, final String[] arguments) {
    this.mainClass = mainClass;
    this.args = args;
    this.options = options == null ? Collections.<Option>emptyList() : Collections.<Option>unmodifiableCollection(options);
    this.arguments = arguments;
    for (final Option option : options)
      optionNameToOption.put(option.getName(), option);
  }

  /**
   * @return Returns an array of unnamed arguments, in original order.
   *         Returns null in case there are no unnamed arguments.
   */
  public String[] getArguments() {
    return arguments;
  }

  public Collection<Option> getOptions() {
    return options;
  }

  public String getOption(final String name) {
    final Option options = optionNameToOption.get(name);
    if (options == null || options.getValues().length == 0)
      return null;

    if (options.getValues().length == 1)
      return options.getValues()[0];

    return Arrays.stream(options.getValues()).reduce(String.valueOf(options.getValueSeparator()), String::concat);
  }

  public String[] getOptions(final String name) {
    final Option reqOption = optionNameToOption.get(name);
    return reqOption != null ? reqOption.getValues() : null;
  }

  public void printCommand(final PrintStream ps, final Class<?> callerClass) {
    ps.print("java " + callerClass.getName());
    for (final String arg : args)
      ps.print(" " + arg);
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder(mainClass.getName());
    if (args.length == 0)
      return buffer.toString();

    for (final String arg : args)
      buffer.append(' ').append(arg);

    return buffer.toString();
  }
}