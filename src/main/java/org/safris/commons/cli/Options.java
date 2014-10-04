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
import java.io.PrintWriter;
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

public final class Options {
  static {
    Validator.setSystemValidator(new BindingValidator());
    Validator.getSystemValidator().setValidateOnParse(true);
  }

  @SuppressWarnings("unchecked")
  public static Options parse(final cli_arguments argsDefBinding, final String[] args) throws OptionsException {
    final Set<String> requiredNames = new HashSet<String>();
    final Map<String,String> nameToAltName = new HashMap<String,String>();
    final Map<String,String> shortNameToArgumentName = new HashMap<String,String>();
    final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
    apacheOptions.addOption(null, "help", false, "Print help and usage.");
    final Map<String,cli_arguments._option> cliOptions = new HashMap<String,cli_arguments._option>();
    if (argsDefBinding != null) {
      for (final cli_arguments._option option : argsDefBinding._option()) {
        final cli_arguments._option._name optionName = option._name(0);
        final String longName = optionName._long$() != null ? optionName._long$().text() : null;
        final String shortName = optionName._short$() != null ? optionName._short$().text() : null;
        final String name = longName != null ? longName : shortName;
        if (longName == null && shortName == null)
          throw new OptionsException("longName == null && shortName == null");

        nameToAltName.put(name, shortName != null ? shortName : longName);
        cliOptions.put(name, option);
        OptionBuilder.withLongOpt(name == longName ? longName : null);
        if (option._argument() != null && option._argument().size() != 0) {
          final cli_arguments._option._argument argument = option._argument(0);
          if (argument._use$() == null || cli_arguments._option._argument._use$.OPTIONAL.text().equals(argument._use$().text()))
            OptionBuilder.hasOptionalArg();
          else if (cli_arguments._option._argument._use$.REQUIRED.text().equals(argument._use$().text()))
            OptionBuilder.hasArg();

          final String argumentName = argument._name$().text();
          shortNameToArgumentName.put(shortName, argumentName);
          OptionBuilder.withArgName(argumentName);
        }

        // Record which arguments are required
        if (option._required$() != null && option._required$().text())
          requiredNames.add(longName);

        OptionBuilder.withValueSeparator(option._valueSeparator$() != null && option._valueSeparator$().text() != null ? option._valueSeparator$().text().charAt(0) : ' ');
        // FIXME: Throw an error in case we don't match the condition!
        if (option._description() != null && option._description().size() != 0)
          OptionBuilder.withDescription(option._description(0).text());

        // FIXME: Throw an error in case we don't match the condition!
        if (option._name() != null && option._name().size() != 0 && optionName._short$() != null)
          apacheOptions.addOption(OptionBuilder.create(optionName._short$().text()));
      }
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
      final Set<String> specifiedLongNames = new HashSet<String>();
      org.apache.commons.cli.Option[] optionArray = commandLine.getOptions();
      for (final org.apache.commons.cli.Option option : optionArray) {
        specifiedLongNames.add(option.getLongOpt());
        if ("help".equals(option.getLongOpt())) {
          final HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp(" ", apacheOptions);
          System.exit(1);
        }
        else {
          final String opt = option.getLongOpt() != null ? option.getLongOpt() : option.getOpt();
          if (option.getValue() != null) {
            optionsMap.put(opt, new Option(opt, option.getValue()));
          }
          else {
            optionsMap.put(opt, new Option(opt, "true"));
          }
        }
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
        for (final cli_arguments._option option : argsDefBinding._option()) {
          if (option._name().size() == 0)
            continue;

          final cli_arguments._option._name optionName = option._name(0);
          final String name = !optionName._long$().isNull() ? optionName._long$().text() : optionName._short$().text();
          if (optionsMap.containsKey(name))
            continue;

          final cli_arguments._option cliOption = cliOptions.get(name);
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

    return new Options(optionsMap.values(), arguments, apacheOptions);
  }

  private Map<String,Option> optionMap = null;
  private volatile boolean optionMapInited = false;
  private final Collection<Option> options;
  private final Collection<String> arguments;
  private final org.apache.commons.cli.Options apacheOptions;

  private Options(final Collection<Option> options, final Collection<String> arguments, final org.apache.commons.cli.Options apacheOptions) {
    this.options = Collections.<Option>unmodifiableCollection(options);
    this.arguments = arguments != null ? Collections.<String>unmodifiableCollection(arguments) : null;
    this.apacheOptions = apacheOptions;
  }

  public Collection<String> getArguments() {
    return arguments;
  }

  public Collection<Option> getOptions() {
    return options;
  }

  public String getOption(final String name) {
    Option reqOption;
    if (optionMapInited) {
      reqOption = optionMap.get(name);
      return reqOption != null ? reqOption.getValue() : null;
    }

    synchronized (options) {
      if (optionMapInited) {
        reqOption = optionMap.get(name);
        return reqOption != null ? reqOption.getValue() : null;
      }

      optionMap = new HashMap<String,Option>();
      for (final Option option : options)
        optionMap.put(option.getName(), option);

      optionMapInited = true;
    }

    reqOption = optionMap.get(name);
    return reqOption != null ? reqOption.getValue() : null;
  }

  public String toString() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintWriter printWriter = new PrintWriter(out);
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH, " ", "", apacheOptions, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
    printWriter.flush();
    return new String(out.toByteArray());
  }
}