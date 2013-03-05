package org.boomerang.services.http.commands;

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
	 * Searches for the <code>org.eclipse.equinox.http.jetty</code> {@link Bundle}
	 * and <code>starts</code> if it is not already started or starting.
	 */
	public void start()
	{
		Bundle bundle = getBundle();

		if (bundle != null)
		{
			if (bundle.getState() != Bundle.STARTING && bundle.getState() != Bundle.ACTIVE)
			{
				info("Starting HTTP Server...");

				try
				{
					bundle.start();
				}
				catch (BundleException e)
				{
					error(e);
				}
			}
			else
			{
				info("HTTP Server is already running.");
			}
		}
	}

	public void stop()
	{
		Bundle bundle = getBundle();

		if (bundle != null)
		{
			if (bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE)
			{
				info("Stopping HTTP Server...");

				try
				{
					bundle.stop();
				}
				catch (BundleException e)
				{
					error("Unable to stop bundle", e);
				}
			}
			else
			{
				info("HTTP Server is already stopped.");
			}
		}
	}

	public void restart()
	{
		Bundle bundle = getBundle();

		if (bundle != null)
		{
			info("Restarting HTTP Server...");

			if (bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE)
			{
				info("Stopping HTTP Server...");

				try
				{
					bundle.stop();
				}
				catch (BundleException e)
				{
					error("Unable to stops server", e);
				}
			}
			else
			{
				info("HTTP Server is already stopped.");
			}

			info("Starting HTTP Server...");

			try
			{
				bundle.start();
			}
			catch (BundleException e)
			{
				error("Unable to start server", e);
			}
		}
	}
	
	private void error(String message, Throwable t)
	{
		log(LogService.LOG_ERROR, message, t);
	}
	
	private void error(Throwable t)
	{
		log(LogService.LOG_ERROR, null, t);
	}
	
	private void info(String message)
	{
		log(LogService.LOG_INFO, message, null);
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
