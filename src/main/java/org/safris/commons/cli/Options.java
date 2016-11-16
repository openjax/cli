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
import org.apache.commons.cli.FixedHelpFormatter;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.safris.cf.xsb.runtime.Bindings;
import org.safris.cf.xsb.runtime.ParseException;
import org.safris.commons.cli.xe.$cli_use;
import org.safris.commons.cli.xe.cli_cli;
import org.safris.commons.lang.Arrays;
import org.safris.commons.xml.validator.ValidationException;
import org.safris.maven.common.Log;
import org.xml.sax.InputSource;

public final class Options {
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

  private static void printHelp(org.apache.commons.cli.Options apacheOptions, final cli_cli._arguments cliArguments, final PrintStream ps) {
    final HelpFormatter formatter = new FixedHelpFormatter();
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
    final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
    apacheOptions.addOption(null, "help", false, "Print help and usage.");
    final Map<String,cli_cli._option> options = new HashMap<String,cli_cli._option>();
    int argumentsMinOccurs = 0;
    int argumentsMaxOccurs = 0;
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
          final String longName = optionName._long$().isNull() ? null : optionName._long$().text();
          final String shortName = optionName._short$().isNull() ? null : optionName._short$().text();
          final String name = longName != null ? longName : shortName;
          if (longName == null && shortName == null) {
            Log.error("both [long] and [short] option names are null in cli spec");
            System.exit(1);
          }

          nameToAltName.put(name, shortName != null ? shortName : longName);
          options.put(name, option);
          OptionBuilder.withLongOpt(name == longName ? longName : null);
          // Record which options are required
          if (option._argument() != null && option._argument().size() != 0) {
            final cli_cli._option._argument argument = option._argument(0);
            final boolean isRequired = $cli_use.required.text().equals(argument._use$().text());
            if (isRequired) {
              OptionBuilder.isRequired();
              requiredNames.add(longName);
            }

            final int maxOccurs = argument._maxOccurs$().isNull() ? 1 : "unbounded".equals(argument._maxOccurs$().text()) ? Integer.MAX_VALUE : Integer.parseInt(argument._maxOccurs$().text());
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

            final char valueSeparator = argument._valueSeparator$().text() != null ? argument._valueSeparator$().text().charAt(0) : ' ';
            OptionBuilder.withArgName(formatArgumentName(argument._label$().text(), maxOccurs, valueSeparator));
            OptionBuilder.withValueSeparator(valueSeparator);
            // FIXME: Throw an error in case we don't match the condition!
            if (!option._description(0).isNull())
              OptionBuilder.withDescription(option._description(0).text());
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

        final String opt = option.getLongOpt() != null ? option.getLongOpt() : option.getOpt();
        optionsMap.put(opt, option.getValue() != null ? new Option(opt, option.getValueSeparator(), option.getValues()) : new Option(opt, option.getValueSeparator(), "true"));
      }
    }

    // See if some arguments are missing
    if (requiredNames.size() != 0) {
      if (specifiedLongNames != null)
        requiredNames.removeAll(specifiedLongNames);

      if (requiredNames.size() != 0) {
        final StringBuilder buffer = new StringBuilder();
        for (final String longName : requiredNames) {
          final String shortName = nameToAltName.get(longName);
          buffer.append("\nMissing argument: -").append(shortName).append(",--").append(longName);
        }

        Options.trapPrintHelp(apacheOptions, cliArguments, buffer.substring(1), System.out);
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
    final Option options = optionNameToOption.get(name);
    return options != null && options.values != null && options.values.length > 0 ? Arrays.toString(options.values, options.valueSeparator) : null;
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