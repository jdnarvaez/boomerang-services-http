package org.boomerang.services.http.commands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * OSGi console command implementation that searches for the bundle containing the
 * specified {@link HttpService} and either starts it, stops it, or stops then starts
 * it (restart) accordingly.
 * 
 * @see {@link BundleActivator#start(BundleContext)}
 * @see {@link BundleActivator#stop(BundleContext)}
 * 
 * <p style="font-style:font-face:Helvetica;italic;font-variant:small-caps;font-size:80%">
 * Copyright&copy; 2013 All rights reserved.<br>
 * Creation Date: 5 March 2013
 * </p>
 * 
 * @author Juan Narvaez (jdnarvaez@gmail.com)
 */
public class HttpServiceControlCommand
{
	/**
	 * Symbolic ID of the bundle containing the {@link HttpService}. This is assumed to be the equinox/jetty runtime by default.
	 */
	private static final String BUNDLE_SYMBOLIC_ID;
	
	static
	{
		BUNDLE_SYMBOLIC_ID = System.getProperty("boomerang.http.service.bundle", "org.eclipse.equinox.http.jetty");
	}
	
	/**
	 * {@link BundleContext} used to start the encapsulating {@link Bundle}
	 */
	private BundleContext context;

	/**
	 * Constructor.
	 * 
	 * @param context {@link BundleContext} used to start the encapsulating {@link Bundle}
	 */
	public HttpServiceControlCommand(BundleContext context)
	{
		this.context = context;
	}

	/**
	 * Searches for the <code>org.eclipse.equinox.http.jetty</code> {@link Bundle}
	 * and returns it if found.
	 * 
	 * @return {@link Bundle}
	 */
	private Bundle getBundle()
	{
		for (Bundle bundle : context.getBundles())
		{
			if (bundle.getSymbolicName().equals(BUNDLE_SYMBOLIC_ID))
			{
				return bundle;
			}
		}

		return null;
	}

	/**
	 * Searches for the {@link HttpService} {@link Bundle} and <code>starts</code> if it is not already in a state of
	 * {@link Bundle#STARTING} or {@link Bundle#ACTIVE}.
	 */
	public void start(CommandSession session)
	{
		Bundle bundle = getBundle();

		if (bundle != null)
		{
			if (bundle.getState() != Bundle.STARTING && bundle.getState() != Bundle.ACTIVE)
			{
				info(session, "Starting HTTP Server...");

				try
				{
					bundle.start();
				}
				catch (BundleException e)
				{
					error(session, e);
				}
			}
			else
			{
				info(session, "HTTP Server is already running.");
			}
		}
	}

	/**
	 * Searches for the {@link HttpService} {@link Bundle} and <code>stops</code> if it is in a state of {@link Bundle#STARTING}, 
	 * {@link Bundle#ACTIVE} and not in the process of {@link Bundle#STOPPING}.
	 */
	public void stop(CommandSession session)
	{
		Bundle bundle = getBundle();

		if (bundle != null)
		{
			if ((bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE) && bundle.getState() != Bundle.STOPPING)
			{
				info(session, "Stopping HTTP Server...");

				try
				{
					bundle.stop();
				}
				catch (BundleException e)
				{
					error(session, "Unable to stop bundle", e);
				}
			}
			else
			{
				info(session, "HTTP Server is already stopped.");
			}
		}
	}

	/**
	 * Searches for the {@link HttpService} {@link Bundle} and <code>stops</code> then <code>starts</code> it.s
	 */
	public void restart(CommandSession session)
	{
		stop(session);
		start(session);
	}
	
	private void error(CommandSession session, String message, Throwable t)
	{
		print(session, LogService.LOG_ERROR, message, t);
	}
	
	private void error(CommandSession session, Throwable t)
	{
		print(session, LogService.LOG_ERROR, null, t);
	}
	
	private void info(CommandSession session, String message)
	{
		print(session, LogService.LOG_INFO, message, null);
	}
	
	private void print(CommandSession session, int level, String message, Throwable t)
	{
		try
		{
			session.getConsole().write(message.concat("\r\n").getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			log(LogService.LOG_ERROR, "Error writing to console", t);
		}
		catch (IOException e)
		{
			log(LogService.LOG_ERROR, "Error writing to console", t);
		}
		
		log(level, message, t);
	}
	
	private void log(int level, String message, Throwable t)
	{
		ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
		
		if (ref != null)
		{
			LogService log = context.getService(ref);
			
			if (log != null)
			{
				if (t != null)
				{
					log.log(level, message, t);
				}
				else
				{
					log.log(level, message);
				}
			}
		}
	}
}
