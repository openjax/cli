package org.safris.commons.cli;

public final class Option
{
	private final String name;
	private final String value;

	public Option(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}
}
