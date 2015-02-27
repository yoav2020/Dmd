package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;
import il.ac.mta.bi.dmd.lookup.WhoisLookup;
import il.ac.mta.bi.dmd.lookup.WhoisQueryResults;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.xbill.DNS.Record;

public class ChainRunnerWhoisQuery extends ProcessChain {
	static Logger 					logger = Logger.getLogger(ChainRunnerDnsLookup.class);
	
	/* chain configuration */
	private static final WhoisLookup whoisLookup = new WhoisLookup();
	private static final Integer MAX_QUERIES_IN_REQUEST = 1024;
	private static final String WHOIS_SERVER_NAME = "whois.cymru.com";
	
	/* members used by all chain instances */
	private static final ConcurrentLinkedQueue<ChainRunnerWhoisQuery> inQueue =
			new ConcurrentLinkedQueue<>();
	private static Integer elementsInQueue = 0;
	
	private final Map<String,ChainRunnerWhoisQuery> pendingChainsMap = 
			new HashMap<>();
	
	public ChainRunnerWhoisQuery() {
		setChainName("Whois Lookup");
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("asNum"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("bgpPrefix"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("cc"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("asOnwer"));
	}
	
	@Override
	public void run() {
		inQueue.add(this);
		elementsInQueue ++;
		
		/* create a worker thread to perform the WhoIsQuery
		 * when there's at least 1 pending domain in queue
		 */
		if (elementsInQueue % MAX_QUERIES_IN_REQUEST == 1) {
			Factory.getFactory().getExecutorForCallableTask(new ChainRunnerWhoisQueryWorker());
			elementsInQueue = 1;
		}
		
	}

	/* internal private class for worker thread; returns a Future reference
	 * to the object called so it can be used to check return value */
	
	private class ChainRunnerWhoisQueryWorker implements Callable<Object>{		
		@Override
		public Object call() throws InterruptedException, UnknownHostException, IOException, ParseException {
			try {
				
				/* make sure the request is full before creating
				 *  a new one */
				
				synchronized (inQueue) {
					ChainRunnerWhoisQuery chainRunner = null;
					Set<String> whoIsQueries = new HashSet<>();
					DomainToAnalyze domainToAnalyze = null;
					
					while (inQueue.isEmpty() == false && whoIsQueries.size() < MAX_QUERIES_IN_REQUEST) {
						chainRunner = inQueue.poll();
						domainToAnalyze = chainRunner.domainToAnalyze;
						Record[] records = (Record[]) domainToAnalyze.getPropertiesMap().get("domainRecords");
						
						if (records != null) {
							for (Record record : records) {
								pendingChainsMap.put(record.rdataToString(), chainRunner);
								whoIsQueries.add(record.rdataToString());
							}
						}
					}
						
					ArrayList<String> whoisLookupResponses = 
							whoisLookup.bulkLookup(WHOIS_SERVER_NAME, whoIsQueries);
					ArrayList<WhoisQueryResults> whoisQueryResults =
							whoisLookup.parseWhoisResponse(whoisLookupResponses);
					
					for (WhoisQueryResults results : whoisQueryResults) {
						chainRunner = pendingChainsMap.get(results.getIpAddr());
						domainToAnalyze = chainRunner.domainToAnalyze;
											
						Map<String, Feature> featuresMap = 
								domainToAnalyze.getFeaturesMap();
						
						featuresMap.get("asNum").setValue(Integer.parseInt(results.getAsNum()));
						featuresMap.get("bgpPrefix").setValue(results.getBgpPrefix());
						featuresMap.get("cc").setValue(results.getCc());
						featuresMap.get("asOnwer").setValue(results.getAsOnwer());
					}
				}
				
				/* flush all pending domains */
				for (ChainRunnerWhoisQuery chainRunner : pendingChainsMap.values()) {
					if(chainRunner != ChainRunnerWhoisQuery.this) {
						chainRunner.setStatus(getStatus());
						chainRunner.flush();
					}
				}
				
			} catch (Exception e) {
				logger.error("caught exception ", e);
				setStatus(ProcessingChain.chainStatus.ERROR);
			}
			
			flush();
			
			return this;
		}
	}
}
