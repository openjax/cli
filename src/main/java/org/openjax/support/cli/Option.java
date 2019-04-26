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

package org.openjax.support.cli;

import java.util.Arrays;

/**
 * Class representing an option on the CLI.
 */
public class Option {
  private final String name;
  private final String[] values;
  private final char valueSeparator;

  /**
   * Creates a new {@code Option} with the specified name, value separator, and
   * an array of associated values.
   *
   * @param name The name.
   * @param valueSeparator The value separator.
   * @param values The associated values.
   */
  public Option(final String name, final char valueSeparator, final String ... values) {
    this.name = name;
    this.valueSeparator = valueSeparator;
    this.values = values;
  }

  /**
   * Creates a new {@code Option} with the specified name, value separator, and
   * single associated value.
   *
   * @param name The name.
   * @param values The associated values.
   */
  public Option(final String name, final String ... values) {
    this(name, '\0', values);
  }

  /**
   * @return The name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return The associated values.
   */
  public String[] getValues() {
    return this.values;
  }

  /**
   * @return The value separator.
   */
  public char getValueSeparator() {
    return this.valueSeparator;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this)
      return true;

    if (!(obj instanceof Option))
      return false;

    final Option that = (Option)obj;
    return (name != null ? name.equals(that.name) : that.name == null) && (values != null ? Arrays.equals(values, that.values) : that.values == null);
  }

  @Override
  public int hashCode() {
    return (name != null ? name.hashCode() : 0) + (values != null ? values.hashCode() : 0);
  }
}