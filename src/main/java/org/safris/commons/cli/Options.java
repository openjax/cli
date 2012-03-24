/*  Copyright Safris Software 2008
 *
 *  This code is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.safris.commons.el.ELs;
import org.safris.commons.el.ExpressionFormatException;
import org.safris.commons.xml.validator.Validator;
import org.safris.xml.generator.compiler.runtime.BindingValidator;

public final class Options {
  static {
    Validator.setSystemValidator(new BindingValidator());
    Validator.getSystemValidator().setValidateOnParse(true);
  }

  public static Options parse(cli_arguments argsDefBinding, String[] args) throws OptionsException {
    final Set<String> requiredLongNames = new HashSet<String>();
    final Map<String,String> longNameToShortName = new HashMap<String,String>();
    final Map<String,String> shortNameToArgumentName = new HashMap<String,String>();
    final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
    apacheOptions.addOption("help", "help", false, "Print help and usage.");
    final Map<String,cli_arguments._option> cliOptions = new HashMap<String,cli_arguments._option>();
    if (argsDefBinding != null) {
      for (cli_arguments._option option : argsDefBinding.get_option()) {
        final cli_arguments._option._name name = option.get_name(0);
        final String longName = name.get_long$().getText();
        final String shortName = name.get_short$().getText();
        longNameToShortName.put(longName, shortName);
        cliOptions.put(longName, option);
        OptionBuilder optionBuilder = OptionBuilder.withLongOpt(longName);
        if (option.get_argument() != null && option.get_argument().size() != 0) {
          final cli_arguments._option._argument argument = option.get_argument(0);
          if (argument.get_use$() == null || cli_arguments._option._argument._use$.OPTIONAL.getText().equals(argument.get_use$().getText()))
            optionBuilder = optionBuilder.hasOptionalArg();
          else if (cli_arguments._option._argument._use$.REQUIRED.getText().equals(argument.get_use$().getText()))
            optionBuilder = optionBuilder.hasArg();

          final String argumentName = argument.get_name$().getText();
          shortNameToArgumentName.put(shortName, argumentName);
          optionBuilder = optionBuilder.withArgName(argumentName);
        }

        // Record which arguments are required
        if (option.get_required$() != null && option.get_required$().getText())
          requiredLongNames.add(longName);

        optionBuilder = optionBuilder.withValueSeparator(option.get_valueSeparator$() != null && option.get_valueSeparator$().getText() != null ? option.get_valueSeparator$().getText().charAt(0) : ' ');
        // FIXME: Throw an error in case we dont match the condition!
        if (option.get_description() != null && option.get_description().size() != 0)
          optionBuilder = optionBuilder.withDescription(option.get_description(0).getText());

        // FIXME: Throw an error in case we dont match the condition!
        if (option.get_name() != null && option.get_name().size() != 0 && name.get_short$() != null)
          apacheOptions.addOption(optionBuilder.create(name.get_short$().getText()));
      }
    }

    final Map<String,Option> optionsMap = new HashMap<String,Option>();
    Collection<String> arguments = null;
    if (args != null && args.length != 0) {
      final CommandLineParser parser = new PosixParser();
      CommandLine commandLine = null;
      try {
        commandLine = parser.parse(apacheOptions, args);
      }
      catch (Exception e) {
        throw new OptionsException(e);
      }

      arguments = commandLine.getArgList();
      final Set<String> specifiedLongNames = new HashSet<String>();
      org.apache.commons.cli.Option[] optionArray = commandLine.getOptions();
      for (org.apache.commons.cli.Option option : optionArray) {
        specifiedLongNames.add(option.getLongOpt());
        if ("help".equals(option.getLongOpt())) {
          final HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp(" ", apacheOptions);
          System.exit(1);
        }
        else if (option.getValue() != null) {
          optionsMap.put(option.getLongOpt(), new Option(option.getLongOpt(), option.getValue()));
        }
        else {
          optionsMap.put(option.getLongOpt(), new Option(option.getLongOpt(), "true"));
        }
      }

      // See if some arguments are missing
      if (requiredLongNames.size() != 0) {
        requiredLongNames.removeAll(specifiedLongNames);
        if (requiredLongNames.size() != 0) {
          final StringBuffer buffer = new StringBuffer();
          for (String longName : requiredLongNames) {
            final String shortName = longNameToShortName.get(longName);
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
        for (cli_arguments._option option : argsDefBinding.get_option()) {
          if (option.get_name().size() == 0)
            continue;

          final cli_arguments._option._name name = option.get_name(0);
          if (optionsMap.containsKey(name.get_long$().getText()))
            continue;

          final cli_arguments._option cliOption = cliOptions.get(name.get_long$().getText());
          String value = null;
          if (cliOption.get_argument() != null && cliOption.get_argument().size() != 0 && cliOption.get_argument(0).get_default$() != null) {
            value = cliOption.get_argument(0).get_default$().getText();
            value = ELs.dereference(value, System.getenv());
          }
          else {
            continue;
          }

          optionsMap.put(name.get_long$().getText(), new Option(name.get_long$().getText(), value));
        }
      }
      catch (ExpressionFormatException e) {
        System.err.println("Error in bootstrap.xml :" + e.getMessage());
        System.exit(1);
      }
    }

    return new Options(optionsMap.values(), arguments, apacheOptions);
  }

  private Map<String,Option> optionMap = null;
  private volatile boolean optionMapLock = false;
  private final Collection<Option> options;
  private final Collection<String> arguments;
  private final org.apache.commons.cli.Options apacheOptions;

  private Options(Collection<Option> options, Collection<String> arguments, org.apache.commons.cli.Options apacheOptions) {
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

  public Option getOption(String name) {
    if (optionMapLock)
      return optionMap.get(name);

    synchronized (options) {
      if (optionMapLock)
        return optionMap.get(name);

      optionMap = new HashMap<String,Option>();
      for (Option option : options)
        optionMap.put(option.getName(), option);

      optionMapLock = true;
    }

    return optionMap.get(name);
  }

  public String toString() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintWriter printWriter = new PrintWriter(out);
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(printWriter, formatter.defaultWidth, " ", "", apacheOptions, formatter.defaultLeftPad, formatter.defaultDescPad, "");
    printWriter.flush();
    return new String(out.toByteArray());
  }
}
