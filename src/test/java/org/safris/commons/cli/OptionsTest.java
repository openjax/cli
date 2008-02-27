package org.safris.commons.cli;

import java.io.FileInputStream;
import org.junit.Test;
import org.safris.xml.generator.compiler.runtime.Bindings;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

public class OptionsTest
{
	public static void main(String[] args) throws Exception
	{
		final OptionsTest optionTest = new OptionsTest();
		optionTest.testOptions();
	}

	public static void main(Options options)
	{
		assertNotNull(options);
		System.out.println(options.toString());
		System.out.println("------------------------------------------");
		for(Option option : options.getOptions())
			System.out.println("[" + option.getName() + "]: " + option.getValue());
	}

    @Test
	public void testOptions() throws Exception
	{
		final String[] args = new String[]
		{
			"--user", "someuser",
			"-D", "070919",
			"--verbose"
		};

		final CliArguments arguments = (CliArguments)Bindings.parse(new InputSource(new FileInputStream("src/test/resources/xml/cli.xml")));
		final Options options = Options.parse(arguments, args);
		main(options);
		assertEquals("user != someuser", "someuser", options.getOption("user").getValue());
		assertEquals("verbose != true", true, Boolean.parseBoolean(options.getOption("verbose").getValue()));
		assertEquals("date != 070919", "070919", options.getOption("date").getValue());
		assertEquals("silent != null", null, options.getOption("silent").getValue());
	}
}
