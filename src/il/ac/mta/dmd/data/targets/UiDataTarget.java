package il.ac.mta.dmd.data.targets;

import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.IClientHandler;
import il.ac.mta.bi.dmd.common.ServerWrapper;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class UiDataTarget extends DataTarget implements IClientHandler  {
	private Integer listeningPort;
	private ServerWrapper serverWrapper;
	private Cache<String, DomainToAnalyze> cache;
	private static Integer CACHE_MAX_SIZE = 50000;
	
	static Logger logger = Logger.getLogger(UiDataTarget.class);
	
	public UiDataTarget(Integer listeningPort) {
		this.listeningPort = listeningPort;
		this.serverWrapper = new ServerWrapper(listeningPort, this);
		
		serverWrapper.setServerDescription("Ui target service");
		serverWrapper.start();
		
		/* create cache to store classification results */
		cache = CacheBuilder.newBuilder().
				concurrencyLevel(1).
				maximumSize(CACHE_MAX_SIZE).
				expireAfterWrite(60*24, TimeUnit.MINUTES).
				build();
	}

	@Override
	public void handle(BufferedReader in, PrintWriter out) throws Exception {
		out.println("User get domain name class, usage: <domain_name>. To exit, type \"$ exit\"");

		while (true) {
			out.print("> ");
		    String line = in.readLine();
		    
		    if (line.equals("$ exit")) {
		    	out.println("bye!");
		    	return;
		    }
		    
		    DomainToAnalyze domainToAnalyze = cache.getIfPresent(line);
		    
		    if(domainToAnalyze != null) {
		    	out.println("Classification: " + line);
		    	out.println(domainToAnalyze.toStringFull());
		    	continue;
		    }
		    
		    out.println("domain not available: " + line);
		}
	}

	@Override
	public void save(DomainToAnalyze domainToAnalyze) {
		logger.info("saved " + domainToAnalyze.getDomainName() + " to cache");
		cache.put(domainToAnalyze.getDomainName(), domainToAnalyze);
	}

	public Integer getListeningPort() {
		return listeningPort;
	}
}
