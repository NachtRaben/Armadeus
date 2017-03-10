package com.nachtraben.core.commandmodule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Created by NachtRaben on 3/7/2017.
 */
public class AnnotatedCommand extends Command {

	Cmd cmd;
	Object methodHolder;
	Method method;

	int size = 0;

	AnnotatedCommand(Cmd cmd, Object methodHolder, Method method) throws CommandCreationException {
		super(cmd.name(), cmd.format());
		this.cmd = cmd;
		this.methodHolder = methodHolder;
		this.method = method;

		description = cmd.description();
		aliases = cmd.aliases();
		flags = cmd.flags();
		validateMethod();
	}

	private void validateMethod() throws CommandCreationException {
		Parameter[] parameters = method.getParameters();
		//if(!method.getReturnType().equals(boolean.class)) throw new CommandCreationException(this, "Return type was not boolean.");
		switch (parameters.length) {
			case 3:
				size = 3;
				if (!Map.class.isAssignableFrom(parameters[2].getType()))
					throw new CommandCreationException(this, "Parameter[2] was not assignable from Map<String, String>.");
				if (parameters[2].getParameterizedType() == null || !parameters[2].getParameterizedType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.String>"))
					throw new CommandCreationException(this, "Parameter[2] was not assignable from Map<String, String>.");
			case 2:
				if (size < 2)
					size = 2;
				if (!Map.class.isAssignableFrom(parameters[1].getType()))
					throw new CommandCreationException(this, "Parameter[1] was not assignable from Map<String, String>.");
				if (parameters[1].getParameterizedType() == null || !parameters[1].getParameterizedType().getTypeName().equals("java.util.Map<java.lang.String, java.lang.String>"))
					throw new CommandCreationException(this, "Parameter[1] was not assignable from Map<String, String>.");
			case 1:
				if (size < 1)
					size = 1;
				if (!parameters[0].getType().equals(CommandSender.class))
					throw new CommandCreationException(this, "Parameter[0] was not CommandSender.class.");
				break;
			default:
				throw new CommandCreationException(this, "Invalid number of parameters");
		}
	}

	@Override
	public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		try {
			switch (size) {
				case 3:
					method.invoke(methodHolder, sender, args, flags);
					break;
				case 2:
					method.invoke(methodHolder, sender, args);
					break;
				case 1:
					method.invoke(methodHolder, sender);
					break;
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}
}
