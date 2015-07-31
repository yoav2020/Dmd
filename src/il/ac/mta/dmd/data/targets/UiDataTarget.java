package il.ac.mta.dmd.data.targets;

import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.IClientHandler;
import il.ac.mta.bi.dmd.common.ProcessingChain;
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
		    boolean sleepOn = false;
		    
		    if (line.equals("$ exit")) {
		    	out.println("bye!");
		    	return;
		    }
		    if (line.startsWith("$ sleep ")) {
		    	line = line.replaceFirst("\\$ sleep\\s+", "");
		    	sleepOn = true;
		    }
		    if (line.isEmpty()) {
		    	continue;
		    }
		    
		    DomainToAnalyze domainToAnalyze = null;
		    long start = System.currentTimeMillis();
		    while (true) {
		    	domainToAnalyze = getGlobalCache().getIfPresent(line);
			    if(domainToAnalyze != null && 
			    		domainToAnalyze.getClassification() != DomainToAnalyze.Classification.UNKNOWN &&
			    		domainToAnalyze.getRunStatus() != ProcessingChain.chainStatus.ERROR) {
			    	out.println("Classification: " + line);
			    	out.println(domainToAnalyze.toStringFull());
			    	break;
			    }
			    if (sleepOn) { Thread.sleep(500); }
			    else { break; }
		    }
		    if (domainToAnalyze == null) {
		    	out.println("domain not available");
		    }
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
