package il.ac.mta.bi.dmd.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

public abstract class ProcessChain {
	static Logger logger = Logger.getLogger(ProcessChain.class);

	private String chainName;
	private ProcessingChain.chainStatus status = ProcessingChain.chainStatus.OK;
	private Queue<DomainToAnalyze> dispatchQueue;
	protected DomainToAnalyze domainToAnalyze;
	protected List<Feature> chainFeaturesList = new ArrayList<>();
	
	public abstract void run();
	
	protected void flush() {
		logger.info("putting back: " + domainToAnalyze.getDomainName() +
					" instance=" + domainToAnalyze + " (chain=" + getChainName() +")");
		dispatchQueue.add(domainToAnalyze);
	}
	
	public String getChainName() {
		return chainName;
	}
	
	public void setChainName(String chainName) {
		this.chainName = chainName;
	}

	public ProcessingChain.chainStatus getStatus() {
		return status;
	}

	protected void setStatus(ProcessingChain.chainStatus status) {
		this.status = status;
	}

	public void setDomainToAnalyze(DomainToAnalyze domainToAnalyze) {
		this.domainToAnalyze = domainToAnalyze;
	}

	public List<Feature> getFeatureList() {
		return chainFeaturesList;
	}

	public Queue<DomainToAnalyze> getDispatchQueue() {
		return dispatchQueue;
	}

	public void setDispatchQueue(Queue<DomainToAnalyze> dispatchQueue) {
		this.dispatchQueue = dispatchQueue;
	}
}
