package il.ac.mta.bi.dmd.common;

import il.ac.mta.bi.dmd.infra.Factory;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public abstract class DataSource {
	static Logger logger = Logger.getLogger(DataSource.class);

	private String sourceName;
	private LinkedBlockingQueue<DomainToAnalyze> dispatchQueue;
	
	public abstract void run();

	public String getSourceName() {
		return sourceName;
	}

	public LinkedBlockingQueue<DomainToAnalyze> getDispatchQueue() {
		return dispatchQueue;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public void setDispatchQueue(LinkedBlockingQueue<DomainToAnalyze> dispatchQueue) {
		this.dispatchQueue = dispatchQueue;
	}
	
	public void addDomainFromSource(String input) {
		String domainName = input.split(" ")[0];
		String classification = null;
		
		if (input.split(" ").length == 2) {
			classification = input.split(" ")[1];
		} else {
			classification = "UNKNOWN";
		}
		
		DomainToAnalyze domainToAnalyze=
		Factory.getFactory().getDomainToAnalyze(domainName, classification);
		getDispatchQueue().add(domainToAnalyze);
	}
	
}
