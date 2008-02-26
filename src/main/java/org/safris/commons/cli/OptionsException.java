package org.safris.commons.cli;

public class OptionsException extends Exception
{
	public OptionsException()
	{
		super();
	}

	public OptionsException(String message)
	{
		super(message);
	}

	public OptionsException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public OptionsException(Throwable cause)
	{
		super(cause);
	}
}
