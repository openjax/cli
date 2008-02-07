package org.safris.commons.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.PosixParser;
import org.safris.commons.el.ELs;
import org.safris.commons.el.ExpressionFormatException;
import org.safris.xml.generator.compiler.util.DefaultValidator;
import org.safris.xml.generator.compiler.util.Validator;

public final class Options
{
    static
	{
        Validator.setSystemValidator(new DefaultValidator());
        Validator.getSystemValidator().setValidateOnParse(true);
    }

    public static Options parse(CliArguments cliArguments, String[] args) throws ArgumentException
	{
        final org.apache.commons.cli.Options apacheOptions = new org.apache.commons.cli.Options();
        apacheOptions.addOption("help", "help", false, "Print help and usage.");
        apacheOptions.addOption("ping", "ping", false, "Get a heartbeat from the application.");
        final Map<String, CliArguments.CliOption> bgiOptions = new HashMap<String, CliArguments.CliOption>();
        for(CliArguments.CliOption option : cliArguments.getCliOption())
		{
            bgiOptions.put(option.getCliName().getCliLongAttr().getTEXT(), option);
            OptionBuilder optionBuilder = OptionBuilder.withLongOpt(option.getCliName().getCliLongAttr().getTEXT());
            if(option.getCliArgument() != null)
			{
                if(CliArguments.CliOption.CliArgument.CliUseAttr.REQUIRED.getValue().equals(option.getCliArgument().getCliUseAttr().getValue()))
                    optionBuilder = optionBuilder.hasArg();
                else
                if(CliArguments.CliOption.CliArgument.CliUseAttr.OPTIONAL.getValue().equals(option.getCliArgument().getCliUseAttr().getValue()))
                    optionBuilder = optionBuilder.hasOptionalArg();

                optionBuilder = optionBuilder.withArgName(option.getCliArgument().getCliNameAttr().getTEXT());
            }

            optionBuilder = optionBuilder.isRequired(option.getCliRequiredAttr() != null && option.getCliRequiredAttr().getValue());
            optionBuilder = optionBuilder.withValueSeparator(option.getCliValueSeparatorAttr() != null ? option.getCliValueSeparatorAttr().getTEXT().charAt(0) : ' ');
            optionBuilder = optionBuilder.withDescription(option.getCliDescription().getTEXT());
            apacheOptions.addOption(optionBuilder.create(option.getCliName().getCliShortAttr().getTEXT()));
        }

        final Map<String, Option> optionsMap = new HashMap<String, Option>();
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
                throw new ArgumentException(e);
            }

            arguments = commandLine.getArgList();
            org.apache.commons.cli.Option[] optionArray = commandLine.getOptions();
            for(org.apache.commons.cli.Option option : optionArray)
			{
                if("help".equals(option.getLongOpt()))
				{
                    final HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp(" ", apacheOptions);
                    System.exit(1);
				}
				else if("ping".equals(option.getLongOpt()))
				{
					Options.heartbeat();
					System.exit(0);
				}
				else if(option.getValue() != null)
                    optionsMap.put(option.getLongOpt(), new Option(option.getLongOpt(), option.getValue()));
                else
                    optionsMap.put(option.getLongOpt(), new Option(option.getLongOpt(), "true"));
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
        catch(ExpressionFormatException efe)
		{
            System.err.println("Error in bootstrap.xml :" + efe.getMessage());
            System.exit(1);
        }
        return new Options(optionsMap.values(), arguments, apacheOptions);
    }

	private static void heartbeat()
	{
		StackTraceElement[] stackArray = Thread.currentThread().getStackTrace();
        StringBuffer buffer = new StringBuffer();
        for(StackTraceElement stackTraceElement : stackArray)
		{
            buffer.append(stackTraceElement.toString()).append("\n");
        }
        System.out.println(buffer.toString());
	}

    private final Map<String, Option> optionMap = new HashMap<String, Option>();
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
