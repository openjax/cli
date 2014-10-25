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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.safris.commons.el.ELs;
import org.safris.commons.el.ExpressionFormatException;
import org.safris.commons.xml.validator.Validator;
import org.safris.xml.generator.compiler.runtime.BindingValidator;
import org.safris.xml.generator.compiler.runtime.Bindings;
import org.xml.sax.InputSource;

public final class Options {
  static {
    Validator.setSystemValidator(new BindingValidator());
    Validator.getSystemValidator().setValidateOnParse(true);
  }

  private static void printHelp(org.apache.commons.cli.Options apacheOptions, final cli_cli._arguments cliArguments, final PrintStream ps) {
    final HelpFormatter formatter = new HelpFormatter();
    final PrintWriter pw = new PrintWriter(ps);
    String args = apacheOptions.getOptions().size() > 0 ? " [options]" : "";
    if (cliArguments != null && !cliArguments.isNull()) {
      for (int i = 1; i <= cliArguments._minOccurs$().text(); i++)
        args += " <" + cliArguments._label$().text() + (i != 1 ? i : "") + ">";

      final boolean maxUnbounded = "unbounded".equals(cliArguments._maxOccurs$().text());
      final int argsMax = maxUnbounded ? 2 + cliArguments._minOccurs$().text(): Integer.parseInt(cliArguments._maxOccurs$().text());
      for (int i = cliArguments._minOccurs$().text() + 1; i <= argsMax; i++)
        args += " [" + cliArguments._label$().text() + (i != 1 ? i : "") + "]";

      if (maxUnbounded)
        args += " [...]";
    }

    formatter.printHelp(pw, formatter.defaultWidth, " ", args.substring(1), apacheOptions, formatter.defaultLeftPad, formatter.defaultDescPad, null, false);
    pw.flush();
  }

  private static void trapPrintHelp(org.apache.commons.cli.Options apacheOptions, final cli_cli._arguments cliArguments, final PrintStream ps) {
    printHelp(apacheOptions, cliArguments, ps);
    System.exit(1);
  }

  public static Options parse(final File cliFile, final String[] args) throws OptionsException {
    try {
      return parse((cli_cli)Bindings.parse(new InputSource(new FileInputStream(cliFile))), args);
    }
    catch (final Exception e) {
      throw new OptionsException(e);
    }
  }

  public static Options parse(final URL cliURL, final String[] args) throws OptionsException {
    try {
      return parse((cli_cli)Bindings.parse(new InputSource(cliURL.openStream())), args);
    }
    catch (final Exception e) {
      throw new OptionsException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static Options parse(final cli_cli argsDefBinding, final String[] args) throws OptionsException {
    final Set<String> requiredNames = new HashSet<String>();
    final Map<String,String> nameToAltName = new HashMap<String,String>();
    final Map<String,String> shortNameToArgumentName = new HashMap<String,String>();
    final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
    apacheOptions.addOption(null, "help", false, "Print help and usage.");
    final Map<String,cli_cli._option> cliOptions = new HashMap<String,cli_cli._option>();
    Integer argumentsMinOccurs = null;
    Integer argumentsMaxOccurs = null;
    final cli_cli._arguments cliArguments;
    if (argsDefBinding != null) {
      cliArguments = argsDefBinding._arguments(0);
      if (!cliArguments.isNull()) {
        argumentsMinOccurs = cliArguments._minOccurs$().text();
        argumentsMaxOccurs = "unbounded".equals(cliArguments._maxOccurs$().text()) ? Integer.MAX_VALUE : Integer.parseInt(cliArguments._maxOccurs$().text());
        if (argumentsMaxOccurs < argumentsMinOccurs) {
          System.err.println("[ERROR] minOccurs > maxOccurs on <arguments> element");
          System.exit(1);
        }
      }

      for (final cli_cli._option option : argsDefBinding._option()) {
        final cli_cli._option._name optionName = option._name(0);
        final String longName = optionName._long$() != null ? optionName._long$().text() : null;
        final String shortName = optionName._short$() != null ? optionName._short$().text() : null;
        final String name = longName != null ? longName : shortName;
        if (longName == null && shortName == null) {
          System.err.println("[ERROR] both [long] and [short] option names are null in cli spec");
          System.exit(1);
        }

        nameToAltName.put(name, shortName != null ? shortName : longName);
        cliOptions.put(name, option);
        OptionBuilder.withLongOpt(name == longName ? longName : null);
        if (option._argument() != null && option._argument().size() != 0) {
          final cli_cli._option._argument argument = option._argument(0);
          if (argument._use$() == null || cli_cli._option._argument._use$.OPTIONAL.text().equals(argument._use$().text()))
            OptionBuilder.hasOptionalArgs();
          else if (cli_cli._option._argument._use$.REQUIRED.text().equals(argument._use$().text()))
            OptionBuilder.hasArgs();

          String argumentName = argument._label$().text();
          shortNameToArgumentName.put(shortName, argumentName);
          if (!option._valueSeparator$().isNull())
            argumentName += option._valueSeparator$().text() + argumentName + option._valueSeparator$().text() + "...";

          OptionBuilder.withArgName(argumentName);
        }

        // Record which arguments are required
        if (option._required$() != null && option._required$().text())
          requiredNames.add(longName);

        OptionBuilder.withValueSeparator(option._valueSeparator$().text() != null ? option._valueSeparator$().text().charAt(0) : ' ');
        // FIXME: Throw an error in case we don't match the condition!
        if (option._description() != null && option._description().size() != 0)
          OptionBuilder.withDescription(option._description(0).text());

        // FIXME: Throw an error in case we don't match the condition!
        if (option._name() != null && option._name().size() != 0 && optionName._short$() != null)
          apacheOptions.addOption(OptionBuilder.create(optionName._short$().text()));
      }
    }
    else {
      cliArguments = null;
    }

    final Map<String,Option> optionsMap = new HashMap<String,Option>();
    Collection<String> arguments = null;
    if (args != null && args.length != 0) {
      final CommandLineParser parser = new PosixParser();
      CommandLine commandLine = null;
      do {
        try {
          commandLine = parser.parse(apacheOptions, args);
        }
        catch (final UnrecognizedOptionException e) {
          String unrecognizedOption;
          if (e.getMessage().startsWith("Unrecognized option: ")) {
            unrecognizedOption = e.getMessage().substring(21);
            System.err.println("Unrecognized option: " + unrecognizedOption);
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

      arguments = commandLine.getArgList();
      if (arguments != null && arguments.size() > 0) {
        if (argumentsMaxOccurs == null || argumentsMinOccurs == null || argumentsMaxOccurs < arguments.size() || arguments.size() < argumentsMinOccurs) {
          Options.trapPrintHelp(apacheOptions, cliArguments, System.err);
        }
      }
      else if (argumentsMaxOccurs != null || argumentsMinOccurs != null) {
        Options.trapPrintHelp(apacheOptions, cliArguments, System.err);
      }

      final Set<String> specifiedLongNames = new HashSet<String>();
      org.apache.commons.cli.Option[] optionArray = commandLine.getOptions();
      for (final org.apache.commons.cli.Option option : optionArray) {
        specifiedLongNames.add(option.getLongOpt());
        if ("help".equals(option.getLongOpt()))
          Options.trapPrintHelp(apacheOptions, cliArguments, System.out);

        final String opt = option.getLongOpt() != null ? option.getLongOpt() : option.getOpt();
        optionsMap.put(opt, option.getValue() != null ? new Option(opt, option.getValues()) : new Option(opt, "true"));
      }

      // See if some arguments are missing
      if (requiredNames.size() != 0) {
        requiredNames.removeAll(specifiedLongNames);
        if (requiredNames.size() != 0) {
          final StringBuffer buffer = new StringBuffer();
          for (final String longName : requiredNames) {
            final String shortName = nameToAltName.get(longName);
            final String argumentName = shortNameToArgumentName.get(shortName);
            buffer.append("\nMissing argument: -").append(shortName).append(",--").append(longName);
            if (argumentName != null)
              System.err.println(" <" + argumentName + ">");
            else
              System.err.println();
          }

          throw new MissingOptionException(buffer.toString());
        }
      }
    }

    // Take care of the default values for unspecified options!
    if (argsDefBinding != null) {
      try {
        for (final cli_cli._option option : argsDefBinding._option()) {
          if (option._name().size() == 0)
            continue;

          final cli_cli._option._name optionName = option._name(0);
          final String name = !optionName._long$().isNull() ? optionName._long$().text() : optionName._short$().text();
          if (optionsMap.containsKey(name))
            continue;

          final cli_cli._option cliOption = cliOptions.get(name);
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
        System.err.println("Error in bootstrap.xml :" + e.getMessage());
        System.exit(1);
      }
    }

    return new Options(args, optionsMap.values(), apacheOptions, arguments.toArray(new String[arguments.size()]), cliArguments);
  }

  private final String[] args;
  private Map<String,Option> optionMap = null;
  private volatile boolean optionMapInited = false;
  private final Collection<Option> options;
  private final org.apache.commons.cli.Options apacheOptions;
  private final String[] arguments;
  private final cli_cli._arguments cliArguments;

  private Options(final String[] args, final Collection<Option> options, final org.apache.commons.cli.Options apacheOptions, final String[] arguments, final cli_cli._arguments cliArguments) {
    this.args = args;
    this.options = Collections.<Option>unmodifiableCollection(options);
    this.apacheOptions = apacheOptions;
    this.arguments = arguments;
    this.cliArguments = cliArguments;
  }

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
    Option reqOption;
    if (optionMapInited) {
      reqOption = optionMap.get(name);
      return reqOption != null ? reqOption.values : null;
    }

    synchronized (options) {
      if (optionMapInited) {
        reqOption = optionMap.get(name);
        return reqOption != null ? reqOption.values : null;
      }

      optionMap = new HashMap<String,Option>();
      for (final Option option : options)
        optionMap.put(option.name, option);

      optionMapInited = true;
    }

    reqOption = optionMap.get(name);
    return reqOption != null ? reqOption.values : null;
  }

  public void printCommand(final PrintStream ps) {
    ps.print("java " + getClass().getSimpleName());
    for (final String arg : args)
      ps.print(" " + arg);
  }

  public String toString() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    printHelp(apacheOptions, cliArguments, new PrintStream(out));
    return new String(out.toByteArray());
  }
}