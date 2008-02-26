package org.safris.commons.cli;

public class MissingOptionException extends OptionsException
{
	public MissingOptionException()
	{
		super();
	}

	public MissingOptionException(String message)
	{
		super();
	}

	public MissingOptionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public MissingOptionException(Throwable cause)
	{
		super(cause);
	}
}
