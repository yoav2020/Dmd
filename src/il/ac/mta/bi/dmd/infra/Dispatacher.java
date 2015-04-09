package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class Dispatacher {
	private LinkedBlockingQueue<DomainToAnalyze> dispatchQueue;
	static Logger logger = Logger.getLogger(Dispatacher.class);
	
	public Dispatacher(LinkedBlockingQueue<DomainToAnalyze> dispatchQueue) {
		this.dispatchQueue = dispatchQueue;
	}
	
	public void run() {
		int total = 0;
		int total_errors = 0;
		
		while(true) {
			try {
				DomainToAnalyze domainToAnalyze = dispatchQueue.take();
				
				ProcessingChain chain = domainToAnalyze.getChain();
				ProcessChain nextChain = chain.next();
				
				if (nextChain == null) {
					if (chain.getStatus() == ProcessingChain.chainStatus.ERROR) {
						total_errors ++;
						logger.warn(domainToAnalyze.getDomainName() + " handling finished with errors, last chain is: " +
								chain.getLastChain().getChainName());
					} else {
						logger.info(domainToAnalyze.getDomainName() + " handling finished successfully");
					}
					
					total ++;
					System.out.println("total domains classified: " + total + 
							"; errors: " + 
							total_errors + 
							"; remaining in queue: " + 
							dispatchQueue.size());
					continue;
				}
	
				logger.info(domainToAnalyze.getDomainName() + " next chain is " + 
				"(" + nextChain.getChainName() + ")");
				
				nextChain.setDispatchQueue(dispatchQueue);
				nextChain.setDomainToAnalyze(domainToAnalyze);
			
				nextChain.run();
			} catch(Exception e) {
				logger.error("caught exception while running chain", e);
			}
		}
	}

	public LinkedBlockingQueue<DomainToAnalyze> getDispatchQueue() {
		return dispatchQueue;
	}

	public void setDispatchQueue(LinkedBlockingQueue<DomainToAnalyze> dispatchQueue) {
		this.dispatchQueue = dispatchQueue;
	}

}
