package il.ac.mta.bi.dmd.common;

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
	
}
