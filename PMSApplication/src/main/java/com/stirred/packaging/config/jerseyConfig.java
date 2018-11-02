package com.stirred.packaging.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.stirred.packaging.EndPointfilter.CORSFilter;
import com.stirred.packaging.Endpoint.ExternalErrorEndPoint;
import com.stirred.packaging.Endpoint.GlobalVariables;
import com.stirred.packaging.Endpoint.HistoryViewEndPoint;
import com.stirred.packaging.Endpoint.MasterJsonFileEndPoint;
import com.stirred.packaging.Endpoint.ReportsEndPoint;
import com.stirred.packaging.Endpoint.UserEndPoint;
import com.stirred.packaging.Endpoint.WorkFlowEndPoint;
import com.stirred.packaging.Endpoint.PackagingTaskFirstTableEndPoint;
import com.stirred.packaging.Endpoint.PackagingTaskSecondTableEndPoint;

/**
 * This class is used to configure Jersey.
 * Whenever we create a new controller or endpoint class, that class should register here. Else API will not work.
 */
@Component
public class jerseyConfig extends ResourceConfig
{
	public jerseyConfig(){
		register(UserEndPoint.class);
		register(CORSFilter.class);
		register(PackagingTaskFirstTableEndPoint.class);
		register(PackagingTaskSecondTableEndPoint.class);
		register(MasterJsonFileEndPoint.class);
		register(WorkFlowEndPoint.class);
		register(HistoryViewEndPoint.class);
		register(ExternalErrorEndPoint.class);
		register(GlobalVariables.class);

		register(ReportsEndPoint.class);
	}

}
