package il.ac.mta.dmd.data.targets;

import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.IClientHandler;
import il.ac.mta.bi.dmd.common.SimpleServer;

import java.io.BufferedReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

public class UiDataTarget extends DataTarget implements IClientHandler  {
	private Integer listeningPort;
	private SimpleServer serverWrapper;
	
	static Logger logger = Logger.getLogger(UiDataTarget.class);
	
	public UiDataTarget(Integer listeningPort) {
		this.listeningPort = listeningPort;
		this.serverWrapper = new SimpleServer(listeningPort, this);
		
		serverWrapper.setServerDescription("Ui target service");
		serverWrapper.start();
	}

	@Override
	public void handle(BufferedReader in, PrintWriter out) throws Exception {
		out.println("User get domain name class, usage: <domain_name>. To exit, type \"$ exit\"");

		while (true) {
		    String line = in.readLine();
		    
		    if (line.equals("$ exit")) {
		    	out.println("bye!");
		    	return;
		    }
		    if (line.isEmpty()) {
		    	continue;
		    }
		    
		    DomainToAnalyze domainToAnalyze = getGlobalCache().getIfPresent(line);
		    
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
		return;
	}

	public Integer getListeningPort() {
		return listeningPort;
	}
}
