package org.boomerang.services.http;

import java.util.Hashtable;

import org.boomerang.services.http.commands.HttpServiceControlCommand;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;


/**
 * Simple {@link BundleActivator} that registers an OSGi console command to start, stop, and restart
 * an {@link HttpService}
 * 
 * <p style="font-style:font-face:Helvetica;italic;font-variant:small-caps;font-size:80%">
 * Copyright&copy; 2013 All rights reserved.<br>
 * Creation Date: 5 March 2013
 * </p>
 * 
 * @author Juan Narvaez (jdnarvaez@gmail.com)
 */
public class Activator implements BundleActivator
{	
	/**
	 * Command scope.
	 */
	public static final String COMMAND_SCOPE = "http";
	
	/**
	 * Supported command keywords.
	 */
	public static final String[] COMMANDS = new String[] { "start", "stop", "restart" };
	
	/**
	 * {@link ServiceRegistration} for the control command
	 */
	private ServiceRegistration<HttpServiceControlCommand> registration = null;

	@Override
	public void start(final BundleContext context) throws Exception
	{
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", COMMAND_SCOPE);
		props.put("osgi.command.function", COMMANDS);
		registration = context.registerService(HttpServiceControlCommand.class, new HttpServiceControlCommand(context), props);
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		if (registration != null)
		{
			registration.unregister();
		}
	}
}
