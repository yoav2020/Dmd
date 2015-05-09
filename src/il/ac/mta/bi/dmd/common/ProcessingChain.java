package il.ac.mta.bi.dmd.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class ProcessingChain {
	private List<ProcessChain> 		chain;
	private Iterator<ProcessChain>	iterator;
	private chainStatus 			status; 
	private ProcessChain 			currChain;
	
	static Logger logger = Logger.getLogger(ProcessingChain.class);
	
	public enum chainStatus {
		OK,
		ERROR
	}

	public ProcessingChain() {
		chain = new ArrayList<ProcessChain>();
		status = chainStatus.OK;
	}
	
	public boolean isFirstChain() {
		return iterator == null;
	}
	
	public boolean isLastChain() {
		return iterator.hasNext() == false;
	}
	
	public void addToChain(ProcessChain processChain) {
		chain.add(processChain);
	}
	
	public void removeFromChain(ProcessChain processChain) {
		chain.remove(processChain);
	}
	
	public ProcessChain getLastChain() {
		return currChain;
	}
	
	public void resetChain() {
		iterator = chain.listIterator();
		status = chainStatus.OK;
	}
	
	public void fastForward() {
		if (iterator == null) {
			iterator = chain.listIterator();
		}
		while(iterator.hasNext()) {
			iterator.next();
		}
	}
	
	public void fastForward(String chainName) {
		boolean found = false;
		
		if (iterator == null) {
			iterator = chain.listIterator();
		}
		
		while(iterator.hasNext() && found == false) {
			if (iterator.next().getChainName().contains(chainName)) {
				logger.info("fast forward to chain= " + chainName);
				found = true;
			}
		}
	}
	
	public ProcessChain next() {
		ProcessChain next = null;
		
		if (iterator == null) {
			iterator = chain.listIterator();
		}
	
		if (currChain != null && currChain.getStatus() == chainStatus.ERROR) {
			status = chainStatus.ERROR;
		}
		else if (status != chainStatus.ERROR && iterator.hasNext()) {
				currChain = next = (ProcessChain) iterator.next();
		}
		
		return next;
	}

	public List<ProcessChain> getChain() {
		return chain;
	}

	public chainStatus getStatus() {
		return status;
	}

	public ProcessChain getCurrChain() {
		return currChain;
	}

	public void setStatus(chainStatus status) {
		this.status = status;
	}	
	
	public ArrayList<Feature> getAllFeaturesFromChain() {
		ArrayList<Feature> allFeaturesFromChain = new ArrayList<Feature>();
		for(ProcessChain nextChain : chain) {
			allFeaturesFromChain.addAll(nextChain.getFeatureList());
		}
		return allFeaturesFromChain;
	}
}
