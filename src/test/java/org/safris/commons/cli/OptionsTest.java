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
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.safris.commons.lang.Resources;
import org.safris.commons.test.LoggableTest;

public class OptionsTest extends LoggableTest {
  public static void main(final Options options) {
    Assert.assertNotNull(options);
    System.out.println(options.toString());
    System.out.println("------------------------------------------");
    for (final Option option : options.getOptions())
      System.out.println("[" + option.name + "]: " + Arrays.toString(option.values));

    System.out.println("[arguments]: " + Arrays.toString(options.getArguments()));
  }

  @Test
  public void testOptions() throws Exception {
    final String[] args = new String[] {
      "--user", "user1,user2",
      "-D", "070919",
      "-V",
      "file1",
      "file2",
      "file3"
    };

    System.out.print("java " + getClass().getSimpleName());
    for (final String arg : args)
      System.out.print(" " + arg);

    log("\n");
    final Options options = Options.parse(Resources.getResource("xml/cli.xml").getURL(), OptionsTest.class, args);
    main(options);
    Assert.assertArrayEquals("user != [user1, user2]", new String[] {"user1", "user2"}, options.getOptions("user"));
    Assert.assertEquals("verbose != true", true, Boolean.parseBoolean(options.getOption("V")));
    Assert.assertEquals("date != 070919", "070919", options.getOption("date"));
    Assert.assertEquals("silent != null", null, options.getOption("silent"));
    Assert.assertArrayEquals("arguments != [file1, file2, file3]", new String[] {"file1", "file2", "file3"}, options.getArguments());
  }

  @Test
  public void testEmptyOptions() throws Exception {
    final Options options = Options.parse(Resources.getResource("xml/empty.xml").getURL(), OptionsTest.class, new String[0]);
    options.printCommand(System.out, OptionsTest.class);
    Assert.assertEquals(0, options.getOptions().size());
  }

  @Test
  public void testPrintCommand() throws Exception {
    final Options options = Options.parse(Resources.getResource("xml/empty.xml").getURL(), OptionsTest.class, new String[0]);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(baos);
    options.printCommand(ps, OptionsTest.class);
    Assert.assertEquals("java org.safris.commons.cli.OptionsTest", baos.toString());
  }
}