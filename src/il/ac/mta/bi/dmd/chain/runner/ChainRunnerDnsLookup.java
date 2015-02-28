package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;
import il.ac.mta.bi.dmd.lookup.DnsLookup;

import java.util.Map;
import java.util.concurrent.Callable;

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
	
	public ChainRunnerDnsLookup() {
		setChainName("DNS Lookup");
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("domainRecords"));
	}
	
	@Override
	public void run() {
		Factory.getFactory().getExecutorForCallableTask(new ChainRunnerDnsLookupWorker());
	}

	/* internal private class for worker thread; returns a Future reference
	 * to the object called so it can be used to check return value */
	private class ChainRunnerDnsLookupWorker implements Callable<Object>{		
		@Override
		public Object call() throws InterruptedException {
			try {
				Integer numOfRecords = 0;
				
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
				setStatus(ProcessingChain.chainStatus.ERROR);
			}
			
			flush();
			
			return this;
		}
	}
}
