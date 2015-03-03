package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.common.ProcessingChain.chainStatus;
import il.ac.mta.bi.dmd.infra.Factory;
import il.ac.mta.bi.dmd.lookup.DnsLookup;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.xbill.DNS.Record;

/**
 * This chain runner fills the following feature for the domain to analyze:
 * "domainRecords". It's string value is the number of records returned, that is 
 * the number of type entries for the domain, and it's property is an array of 
 * org.xbill.DNS.Record which holds the actual Type A record information for the domain as returned
 * from the DNS server
 */

public class ChainRunnerDnsLookup extends ProcessChain {
	static Logger 					logger = Logger.getLogger(ChainRunnerDnsLookup.class);
	
	private static final DnsLookup	dnsLookup = new DnsLookup();
	private static final String 	DNS_SERVER = "8.8.8.8";
	private static final Integer	MAX_THREAD_COUNT = 32;
	
	private static final LinkedBlockingQueue<ChainRunnerDnsLookup> inQueue =
			new LinkedBlockingQueue<>();
	private static Boolean isConsumerRunnig = false;
	
	public ChainRunnerDnsLookup() {
		setChainName("DNS Lookup");
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("domainRecords"));
		
		if (isConsumerRunnig == false) {
			firstChainInit();
		}
	}
	
	private void firstChainInit() {
		/* init working threads */
		for (int i = 0; i < MAX_THREAD_COUNT; i ++ ) {
			Factory.getFactory().execForRunnableTask(new ChainRunnerDnsLookupWorker());
		}
		isConsumerRunnig = true;
	}
	
	@Override
	public void run() {
		inQueue.add(this);
	}

	/* internal private class for worker thread; returns a Future reference
	 * to the object called so it can be used to check return value */
	private class ChainRunnerDnsLookupWorker implements Runnable{
		@Override
		public void run() {
			while (true) {
				Integer numOfRecords = 0;
				ChainRunnerDnsLookup chainRunner = null;
				chainStatus chainRunnerStatus = ProcessingChain.chainStatus.OK;
				
				try {				
					chainRunner = inQueue.take();
					DomainToAnalyze domainToAnalyze = chainRunner.domainToAnalyze;
					
					Record [] domainRecords = dnsLookup.lookupTypeA(domainToAnalyze.getDomainName(), 
							DNS_SERVER);
					
					if(domainRecords != null) {
						numOfRecords = domainRecords.length;
					}
					
					logger.info("inserting " + numOfRecords + " domain records for " +
							domainToAnalyze.getDomainName());
					
					Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
					Map<String, Object> propertiesMap = domainToAnalyze.getPropertiesMap();
					
					Feature feature = featuresMap.get("domainRecords");
					feature.setValue(numOfRecords);
					featuresMap.put(feature.getName(), feature);
					propertiesMap.put(feature.getName(), domainRecords);
				} catch (Exception e) {
					logger.error("caught exception ", e);
					chainRunnerStatus = ProcessingChain.chainStatus.ERROR;
				}
				finally {
					if (chainRunner != null) {
						chainRunner.setStatus(chainRunnerStatus);
						chainRunner.flush();
					}
				}
			}
		}
	}
}
