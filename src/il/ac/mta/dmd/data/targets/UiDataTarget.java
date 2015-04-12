package il.ac.mta.dmd.data.targets;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.IClientHandler;
import il.ac.mta.bi.dmd.common.ServerWrapper;

public class UiDataTarget extends DataTarget implements IClientHandler  {
	private Integer listeningPort;
	private ServerWrapper serverWrapper;
	private Cache<String, DomainToAnalyze> cache;
	
	public UiDataTarget(Integer listeningPort) {
		this.listeningPort = listeningPort;
		this.serverWrapper = new ServerWrapper(listeningPort, this);
		
		serverWrapper.setServerDescription("Ui target service");
		serverWrapper.start();
		
		/* create cache */
		cache = CacheBuilder.newBuilder().
				maximumSize(1000000).
				expireAfterWrite(60*24, TimeUnit.MINUTES).
				build();
	}

	@Override
	public void handle(BufferedReader in, PrintWriter out) throws Exception {
		/*
		out.println("User load domain names, usage: <domain_name> [classification]. To exit, type \"$ exit\"");

		while (true) {
			out.print("> ");
		    String line = in.readLine();
		    
		    if (line.equals("$ exit")) {
		    	out.println("bye!");
		    	return;
		    }
		    
		    out.println("loaded '" + line + "'");
		    addDomainFromSource(line);
		}*/
	}

	@Override
	public void save(DomainToAnalyze domainToAnalyze) {
		cache.put(domainToAnalyze.getDomainName(), domainToAnalyze);
	}

	public Integer getListeningPort() {
		return listeningPort;
	}
}
