package nl.nurdspace.irc.spacebot;

public final class Command {
	private final String command;
	private final String argumentString;
	private final String[] args;
	
	public Command(final String message) {
		if (!isCommand(message)) {
			throw new IllegalArgumentException("message is geen command: " + message);
		}
		int spaceLocation = message.indexOf(' ');
		if (spaceLocation > 0) {
			this.command = message.substring(1, spaceLocation);
			String arguments = message.substring(spaceLocation).trim();
			if (arguments.length() > 0) {
				this.argumentString = arguments;
				this.args = argumentString.split(" ");
			} else {
				this.argumentString = null;
				this.args = null;
			}
		} else {
			this.command = message.substring(1).trim();
			this.argumentString = null;
			this.args = null;
		}
	}
	
	public int getNumberOfArguments() {
		int number;
		if (args != null) {
			number = args.length;
		} else {
			number = 0;
		}
		return number;
	}
	
	public static boolean isCommand(final String message) {
		return message != null && message.trim().length() > 1 && message.startsWith("!");
	}

	public String getCommand() {
		return command;
	}

	public String getArgumentString() {
		return argumentString;
	}

	public String[] getArgs() {
		return args;
	}
}
