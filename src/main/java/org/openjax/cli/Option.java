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

import java.util.Arrays;
import java.util.Objects;

/**
 * Class representing an option on the CLI.
 */
public class Option {
  private final String name;
  private final String[] values;
  private final char valueSeparator;

  /**
   * Creates a new {@link Option} with the specified name, value separator, and an array of associated values.
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
   * Creates a new {@link Option} with the specified name, value separator, and single associated value.
   *
   * @param name The name.
   * @param values The associated values.
   */
  public Option(final String name, final String ... values) {
    this(name, '\0', values);
  }

  /**
   * Returns the name.
   *
   * @return The name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the associated values.
   *
   * @return The associated values.
   */
  public String[] getValues() {
    return this.values;
  }

  /**
   * Returns the value separator.
   *
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
    return Objects.equals(name, that.name) && (values != null ? Arrays.equals(values, that.values) : that.values == null);
  }

  @Override
  public int hashCode() {
    int hashCode = 1;
    if (name != null)
      hashCode = 31 * hashCode + name.hashCode();

    if (values != null)
      hashCode = 31 * hashCode + values.hashCode();

    return hashCode;
  }
}