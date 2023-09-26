/* Copyright (c) 2008 OpenJAX
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

package org.openjax.cli;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.Test;
import org.libj.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionsTest {
  private static final Logger logger = LoggerFactory.getLogger(OptionsTest.class);

  public static void main(final Options options) {
    assertNotNull(options);
    System.out.println(options.toString());
    System.out.println("------------------------------------------");
    for (final Option option : options.getOptions()) // [C]
      System.out.println("[" + option.getName() + "]: " + Arrays.toString(option.getValues()));

    System.out.println("[arguments]: " + Arrays.toString(options.getArguments()));
  }

  @Test
  public void testOptions() throws Exception {
    final String[] args = {
      "--users", "user1,user2",
      "-V",
      "file1",
      "file2",
      "file3"
    };

    System.out.print("java " + getClass().getSimpleName());
    for (final String arg : args) // [A]
      System.out.print(" " + arg);

    if (logger.isInfoEnabled()) { logger.info("\n"); }
    final Options options = Options.parse(ClassLoader.getSystemClassLoader().getResource("cli.xml"), args);
    main(options);
    assertEquals("config.xml", options.getOption("config"));
    assertArrayEquals("user != [user1, user2]", new String[] {"user1", "user2"}, options.getOptions("users"));
    assertTrue("verbose != true", Boolean.parseBoolean(options.getOption("V")));
    assertNull("silent != null", options.getOption("silent"));
    assertArrayEquals("arguments != [file1, file2, file3]", new String[] {"file1", "file2", "file3"}, options.getArguments());
  }

  @Test
  public void testEmptyOptions() throws Exception {
    final Options options = Options.parse(ClassLoader.getSystemClassLoader().getResource("empty.xml"), Strings.EMPTY_ARRAY);
    options.printCommand(System.out, OptionsTest.class);
    assertEquals(0, options.getOptions().size());
  }

  @Test
  public void testPrintCommand() throws Exception {
    final Options options = Options.parse(ClassLoader.getSystemClassLoader().getResource("empty.xml"), Strings.EMPTY_ARRAY);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(baos);
    options.printCommand(ps, OptionsTest.class);
    assertEquals("java " + OptionsTest.class.getName(), baos.toString());
  }

  @Test
  public void testExecuteSuccess() throws Exception {
    Options.parse(ClassLoader.getSystemClassLoader().getResource("cli.xml"), new String[] {"--config", "config.xml", "--users", "bob,joe", "file1.txt", "file2.txt", "file3.txt"});
  }
}