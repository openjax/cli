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

import java.io.FileInputStream;
import org.junit.Test;
import org.safris.xml.generator.compiler.runtime.Bindings;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

public class OptionsTest {
  public static void main(String[] args) throws Exception {
    final OptionsTest optionTest = new OptionsTest();
    optionTest.testOptions();
  }

  public static void main(Options options) {
    assertNotNull(options);
    System.out.println(options.toString());
    System.out.println("------------------------------------------");
    for (Option option : options.getOptions())
      System.out.println("[" + option.getName() + "]: " + option.getValue());
  }

  @Test
  public void testOptions() throws Exception {
    final String[] args = new String[] {
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
    assertEquals("silent != null", null, options.getOption("silent"));
  }
}
