/* Copyright (c) 2008 Seva Safris
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

package org.safris.commons.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.safris.commons.cli.xe.$cli_use;
import org.safris.commons.cli.xe.cli_cli;
import org.safris.commons.util.ELs;
import org.safris.commons.util.ExpressionFormatException;
import org.safris.commons.xml.validator.ValidationException;
import org.safris.maven.common.Log;
import org.safris.xsb.compiler.runtime.Bindings;
import org.safris.xsb.compiler.runtime.ParseException;
import org.xml.sax.InputSource;

public final class Options {
  private static void printHelp(org.apache.commons.cli.Options apacheOptions, final cli_cli._arguments cliArguments, final PrintStream ps) {
    final HelpFormatter formatter = new HelpFormatter();
    final PrintWriter pw = new PrintWriter(ps);
    final StringBuilder args = new StringBuilder(apacheOptions.getOptions().size() > 0 ? " [options]" : "");
    if (cliArguments != null && !cliArguments.isNull()) {
      for (int i = 1; i <= cliArguments._minOccurs$().text(); i++)
        args.append(" <").append(cliArguments._label$().text()).append((i != 1 ? i : "")).append(">");

      final boolean maxUnbounded = "unbounded".equals(cliArguments._maxOccurs$().text());
      final int argsMax = maxUnbounded ? 2 + cliArguments._minOccurs$().text(): Integer.parseInt(cliArguments._maxOccurs$().text());
      for (int i = cliArguments._minOccurs$().text() + 1; i <= argsMax; i++)
        args.append(" [").append(cliArguments._label$().text()).append((i != 1 ? i : "")).append("]");

      if (maxUnbounded)
        args.append(" [...]");
    }

    formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, " ", args.substring(1), apacheOptions, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, false);
    pw.flush();
  }

  private static void trapPrintHelp(org.apache.commons.cli.Options apacheOptions, final cli_cli._arguments cliArguments, final String message, final PrintStream ps) {
    if (message != null)
      ps.println(message);

    printHelp(apacheOptions, cliArguments, ps);
    System.exit(1);
  }

  public static Options parse(final File cliFile, final Class<?> mainClass, final String[] args) throws OptionsException {
    try {
      return parse((cli_cli)Bindings.parse(new InputSource(new FileInputStream(cliFile))), mainClass, args);
    }
    catch (final IOException | ParseException | ValidationException e) {
      throw new OptionsException(e);
    }
  }

  public static Options parse(final URL cliURL, final Class<?> mainClass, final String[] args) throws OptionsException {
    try {
      return parse((cli_cli)Bindings.parse(new InputSource(cliURL.openStream())), mainClass, args);
    }
    catch (final IOException | ParseException | ValidationException e) {
      throw new OptionsException(e);
    }
  }

  public static Options parse(final cli_cli argsDefBinding, final Class<?> mainClass, final String[] args) throws OptionsException {
    final Set<String> requiredNames = new HashSet<String>();
    final Map<String,String> nameToAltName = new HashMap<String,String>();
    final Map<String,String> shortNameToArgumentName = new HashMap<String,String>();
    final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
    apacheOptions.addOption(null, "help", false, "Print help and usage.");
    final Map<String,cli_cli._option> options = new HashMap<String,cli_cli._option>();
    Integer argumentsMinOccurs = null;
    Integer argumentsMaxOccurs = null;
    final cli_cli._arguments cliArguments;
    if (argsDefBinding != null) {
      cliArguments = argsDefBinding._arguments(0);
      if (!cliArguments.isNull()) {
        argumentsMinOccurs = cliArguments._minOccurs$().text();
        argumentsMaxOccurs = "unbounded".equals(cliArguments._maxOccurs$().text()) ? Integer.MAX_VALUE : Integer.parseInt(cliArguments._maxOccurs$().text());
        if (argumentsMaxOccurs < argumentsMinOccurs) {
          Log.error("minOccurs > maxOccurs on <arguments> element");
          System.exit(1);
        }
      }

      if (argsDefBinding._option() != null) {
        for (final cli_cli._option option : argsDefBinding._option()) {
          final cli_cli._option._name optionName = option._name(0);
          final String longName = optionName._long$() != null ? optionName._long$().text() : null;
          final String shortName = optionName._short$() != null ? optionName._short$().text() : null;
          final String name = longName != null ? longName : shortName;
          if (longName == null && shortName == null) {
            Log.error("both [long] and [short] option names are null in cli spec");
            System.exit(1);
          }

          nameToAltName.put(name, shortName != null ? shortName : longName);
          options.put(name, option);
          OptionBuilder.withLongOpt(name == longName ? longName : null);
          if (option._argument() != null && option._argument().size() != 0) {
            final cli_cli._option._argument argument = option._argument(0);
            if (argument._use$() == null || $cli_use.optional.text().equals(argument._use$().text()))
              OptionBuilder.hasOptionalArgs();
            else if ($cli_use.required.text().equals(argument._use$().text()))
              OptionBuilder.hasArgs();

            final StringBuilder argumentName = new StringBuilder(argument._label$().text());
            shortNameToArgumentName.put(shortName, argumentName.toString());
            if (!option._valueSeparator$().isNull())
              argumentName.append(option._valueSeparator$().text()).append(argumentName).append(option._valueSeparator$().text()).append("...");

            OptionBuilder.withArgName(argumentName.toString());
          }

          // Record which arguments are required
          if ("required".equals(option._use$().text()))
            requiredNames.add(longName);

          OptionBuilder.withValueSeparator(option._valueSeparator$().text() != null ? option._valueSeparator$().text().charAt(0) : ' ');
          // FIXME: Throw an error in case we don't match the condition!
          if (option._description() != null && option._description().size() != 0) {
            final StringBuilder description = new StringBuilder(option._description(0).text());
            if (!option._argument(0).isNull())
              description.append("\ndefault: <").append(option._argument(0)._default$().text()).append(">");

            OptionBuilder.withDescription(description.toString());
          }

          // FIXME: Throw an error in case we don't match the condition!
          if (option._name() != null && option._name().size() != 0 && optionName._short$() != null)
            apacheOptions.addOption(OptionBuilder.create(optionName._short$().text()));
        }
      }
    }
    else {
      cliArguments = null;
    }

    final Map<String,Option> optionsMap = new HashMap<String,Option>();
    final Set<String> specifiedLongNames;
    CommandLine commandLine = null;
    if (args != null && args.length != 0) {
      specifiedLongNames = new HashSet<String>();
      final CommandLineParser parser = new PosixParser();
      do {
        try {
          commandLine = parser.parse(apacheOptions, args);
        }
        catch (final UnrecognizedOptionException e) {
          if (e.getMessage().startsWith("Unrecognized option: ")) {
            final String unrecognizedOption = e.getMessage().substring(21);
            Log.error("Unrecognized option: " + unrecognizedOption);
            for (int i = 0; i < args.length; i++)
              if (args[i].equals(unrecognizedOption))
                args[i] = "--help";
          }
          else {
            throw new OptionsException(e);
          }
        }
        catch (final Exception e) {
          throw new OptionsException(e);
        }
      }
      while (commandLine == null);
    }
    else {
      specifiedLongNames = null;
    }

    final Collection<String> arguments = commandLine != null ? commandLine.getArgList() : null;
    if (arguments != null && arguments.size() > 0) {
      if (argumentsMaxOccurs == null || argumentsMinOccurs == null || argumentsMaxOccurs < arguments.size() || arguments.size() < argumentsMinOccurs) {
        Options.trapPrintHelp(apacheOptions, cliArguments, null, System.err);
      }
    }
    else if (argumentsMinOccurs != null && argumentsMinOccurs > 0) {
      Options.trapPrintHelp(apacheOptions, cliArguments, null, System.err);
    }

    if (commandLine != null) {
      for (final org.apache.commons.cli.Option option : commandLine.getOptions()) {
        specifiedLongNames.add(option.getLongOpt());
        if ("help".equals(option.getLongOpt()))
          Options.trapPrintHelp(apacheOptions, cliArguments, null, System.out);

        final String opt = option.getLongOpt() != null ? option.getLongOpt() : option.getOpt();
        optionsMap.put(opt, option.getValue() != null ? new Option(opt, option.getValues()) : new Option(opt, "true"));
      }
    }

    // See if some arguments are missing
    if (requiredNames.size() != 0) {
      if (specifiedLongNames != null)
        requiredNames.removeAll(specifiedLongNames);

      if (requiredNames.size() != 0) {
        final StringBuffer buffer = new StringBuffer();
        for (final String longName : requiredNames) {
          final String shortName = nameToAltName.get(longName);
          buffer.append("\nMissing argument: -").append(shortName).append(",--").append(longName);
        }

        Options.trapPrintHelp(apacheOptions, cliArguments, buffer.substring(1), System.out);
      }
    }

    // Take care of the default values for unspecified options!
    if (argsDefBinding != null && argsDefBinding._option() != null) {
      try {
        for (final cli_cli._option option : argsDefBinding._option()) {
          if (option._name().size() == 0)
            continue;

          final cli_cli._option._name optionName = option._name(0);
          final String name = !optionName._long$().isNull() ? optionName._long$().text() : optionName._short$().text();
          if (optionsMap.containsKey(name))
            continue;

          final cli_cli._option cliOption = options.get(name);
          String value = null;
          if (cliOption._argument() != null && cliOption._argument().size() != 0 && cliOption._argument(0)._default$() != null) {
            value = cliOption._argument(0)._default$().text();
            value = ELs.dereference(value, System.getenv());
          }
          else {
            continue;
          }

          optionsMap.put(name, new Option(name, value));
        }
      }
      catch (final ExpressionFormatException e) {
        Log.error(e.getMessage(), e);
        System.exit(1);
      }
    }

    return new Options(mainClass, args, optionsMap.values(), arguments == null || arguments.size() == 0 ? null : arguments.toArray(new String[arguments.size()]));
  }

  private final Map<String,Option> optionNameToOption = new HashMap<String,Option>();
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
      optionNameToOption.put(option.name, option);
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
    final String[] options = getOptions(name);
    return options != null && options.length > 0 ? options[0] : null;
  }

  public String[] getOptions(final String name) {
    final Option reqOption = optionNameToOption.get(name);
    return reqOption != null ? reqOption.values : null;
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
      buffer.append(" ").append(arg);

    return buffer.toString();
  }
}