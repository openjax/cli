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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.util.Arrays;

import org.junit.Test;
import org.safris.xml.generator.compiler.runtime.Bindings;
import org.xml.sax.InputSource;

public final class OptionsTest {
  public static void main(final String[] args) throws Exception {
    final OptionsTest optionTest = new OptionsTest();
    optionTest.testOptions();
  }

  public static void main(final Options options) {
    assertNotNull(options);
    System.out.println(options.toString());
    System.out.println("------------------------------------------");
    for (final Option option : options.getOptions())
      System.out.println("[" + option.getName() + "]: " + option.getValue());
  }

  @Test
  public void testOptions() throws Exception {
    final String[] args = new String[] {
      "--user", "someuser",
      "-D", "070919",
      "-V"
    };

    System.out.println("java " + getClass().getSimpleName() + " " + Arrays.toString(args).replaceAll("[\\[\\],]", "") + "\n");
    final cli_arguments arguments = (cli_arguments)Bindings.parse(new InputSource(new FileInputStream("src/test/resources/xml/cli.xml")));
    final Options options = Options.parse(arguments, args);
    main(options);
    assertEquals("user != someuser", "someuser", options.getOption("user"));
    assertEquals("verbose != true", true, Boolean.parseBoolean(options.getOption("V")));
    assertEquals("date != 070919", "070919", options.getOption("date"));
    assertEquals("silent != null", null, options.getOption("silent"));
  }
}