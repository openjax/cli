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
import org.safris.commons.xml.validation.DefaultValidator;
import org.safris.commons.xml.validation.Validator;

public final class Options
{
	static
	{
		Validator.setSystemValidator(new DefaultValidator());
		Validator.getSystemValidator().setValidateOnParse(true);
	}

	public static Options parse(CliArguments cliArguments, String[] args) throws OptionsException
	{
		if(cliArguments == null)
			return null;

		final Set<String> requiredLongNames = new HashSet<String>();
		final Map<String,String> longNameToShortName = new HashMap<String,String>();
		final Map<String,String> shortNameToArgumentName = new HashMap<String,String>();
		final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
		apacheOptions.addOption("help", "help", false, "Print help and usage.");
		final Map<String,CliArguments.CliOption> bgiOptions = new HashMap<String, CliArguments.CliOption>();
		for(CliArguments.CliOption option : cliArguments.getCliOption())
		{
			final String longName = option.getCliName().getCliLongAttr().getTEXT();
			final String shortName = option.getCliName().getCliShortAttr().getTEXT();
			longNameToShortName.put(longName, shortName);
			bgiOptions.put(longName, option);
			OptionBuilder optionBuilder = OptionBuilder.withLongOpt(longName);
			if(option.getCliArgument() != null)
			{
				final String use = option.getCliArgument().getCliUseAttr().getValue();
				if(CliArguments.CliOption.CliArgument.CliUseAttr.REQUIRED.getValue().equals(use))
					optionBuilder = optionBuilder.hasArg();
				else if(CliArguments.CliOption.CliArgument.CliUseAttr.OPTIONAL.getValue().equals(use))
					optionBuilder = optionBuilder.hasOptionalArg();

				final String argumentName = option.getCliArgument().getCliNameAttr().getTEXT();
				shortNameToArgumentName.put(shortName, argumentName);
				optionBuilder = optionBuilder.withArgName(argumentName);
			}

			// Record which arguments are required
			if(option.getCliRequiredAttr() != null && option.getCliRequiredAttr().getValue())
				requiredLongNames.add(longName);

			optionBuilder = optionBuilder.withValueSeparator(option.getCliValueSeparatorAttr() != null ? option.getCliValueSeparatorAttr().getTEXT().charAt(0) : ' ');
			optionBuilder = optionBuilder.withDescription(option.getCliDescription().getTEXT());
			apacheOptions.addOption(optionBuilder.create(option.getCliName().getCliShortAttr().getTEXT()));
		}

		final Map<String,Option> optionsMap = new HashMap<String,Option>();
		Collection<String> arguments = null;
		if(args != null && args.length != 0)
		{
			final CommandLineParser parser = new PosixParser();
			CommandLine commandLine = null;
			try
			{
				commandLine = parser.parse(apacheOptions, args);
			}
			catch(Exception e)
			{
				throw new OptionsException(e);
			}

			arguments = commandLine.getArgList();
			final Set<String> specifiedLongNames = new HashSet<String>();
			org.apache.commons.cli.Option[] optionArray = commandLine.getOptions();
			for(org.apache.commons.cli.Option option : optionArray)
			{
				specifiedLongNames.add(option.getLongOpt());
				if("help".equals(option.getLongOpt()))
				{
					final HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp(" ", apacheOptions);
					System.exit(1);
				}
				else if(option.getValue() != null)
					optionsMap.put(option.getLongOpt(), new Option(option.getLongOpt(), option.getValue()));
				else
					optionsMap.put(option.getLongOpt(), new Option(option.getLongOpt(), "true"));
			}

			// See if some arguments are missing
			if(requiredLongNames.size() != 0)
			{
				requiredLongNames.removeAll(specifiedLongNames);
				if(requiredLongNames.size() != 0)
				{
					final StringBuffer buffer = new StringBuffer();
					for(String longName : requiredLongNames)
					{
						final String shortName = longNameToShortName.get(longName);
						final String argumentName = shortNameToArgumentName.get(shortName);
						buffer.append("\nMissing argument: -").append(shortName).append(",--").append(longName);
						if(argumentName != null)
							System.err.println(" <" + argumentName + ">");
						else
							System.err.println();
					}

					throw new MissingOptionException(buffer.toString());
				}
			}
		}

		try
		{
			for(CliArguments.CliOption option : cliArguments.getCliOption())
			{
				if(!optionsMap.containsKey(option.getCliName().getCliLongAttr().getTEXT()))
				{
					final CliArguments.CliOption cliOption = bgiOptions.get(option.getCliName().getCliLongAttr().getTEXT());
					String value = null;
					if(cliOption.getCliArgument() != null && cliOption.getCliArgument().getCliDefaultAttr() != null)
					{
						value = cliOption.getCliArgument().getCliDefaultAttr().getValue();
						value = ELs.dereference(value, System.getenv());
					}
					else
					{
						value = null;
					}

					optionsMap.put(option.getCliName().getCliLongAttr().getTEXT(), new Option(option.getCliName().getCliLongAttr().getTEXT(), value));
				}
			}
		}
		catch(ExpressionFormatException e)
		{
			System.err.println("Error in bootstrap.xml :" + e.getMessage());
			System.exit(1);
		}

		return new Options(optionsMap.values(), arguments, apacheOptions);
	}

	private final Map<String,Option> optionMap = new HashMap<String,Option>();
	private final Collection<Option> options;
	private final Collection<String> arguments;
	private final org.apache.commons.cli.Options apacheOptions;

	private Options(Collection<Option> options, Collection<String> arguments, org.apache.commons.cli.Options apacheOptions)
	{
		this.options = options;
		this.arguments = arguments;
		this.apacheOptions = apacheOptions;
		for(Option option : options)
			optionMap.put(option.getName(), option);
	}

	public Collection<String> getArguments()
	{
		return arguments;
	}

	public Collection<Option> getOptions()
	{
		return Collections.unmodifiableCollection(options);
	}

	public Option getOption(String name)
	{
		return optionMap.get(name);
	}

	public String toString()
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintWriter printWriter = new PrintWriter(out);
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(printWriter, formatter.defaultWidth, " ", "", apacheOptions, formatter.defaultLeftPad, formatter.defaultDescPad, "");
		printWriter.flush();
		return new String(out.toByteArray());
	}
}
