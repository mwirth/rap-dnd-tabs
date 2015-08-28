package org.wimi.rap.dndtabs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

public class BasicApplication implements ApplicationConfiguration
{

	public void configure(Application application)
	{
		Map<String, String> properties = new HashMap<String, String>();
		application.addEntryPoint("/tabs", BasicEntryPoint.class, properties);
	}
}
