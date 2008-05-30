/*  Copyright 2008 Safris Technologies Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

		final cli_arguments arguments = (cli_arguments)Bindings.parse(new InputSource(new FileInputStream("src/test/resources/xml/cli.xml")));
		final Options options = Options.parse(arguments, args);
		main(options);
		assertEquals("user != someuser", "someuser", options.getOption("user").getValue());
		assertEquals("verbose != true", true, Boolean.parseBoolean(options.getOption("verbose").getValue()));
		assertEquals("date != 070919", "070919", options.getOption("date").getValue());
		assertEquals("silent != null", null, options.getOption("silent").getValue());
	}
}
