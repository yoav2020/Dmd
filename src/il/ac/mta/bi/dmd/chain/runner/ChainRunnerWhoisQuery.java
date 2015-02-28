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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.xbill.DNS.Record;

/**
 * This chain runner fills the following feature for the domain to analyze:
 * "asNum" - the domain address autonomous system, "bgpPrefix" the domain address
 * network and prefix, "cc" - country code, "asOnwer" - the as owner
 */

public class ChainRunnerWhoisQuery extends ProcessChain {
	static Logger 					logger = Logger.getLogger(ChainRunnerDnsLookup.class);
	
	/* chain configuration */
	private static final WhoisLookup whoisLookup = new WhoisLookup();
	private static final Integer MAX_QUERIES_IN_REQUEST = 1024;
	private static final String WHOIS_SERVER_NAME = "whois.cymru.com";
	
	/* members used by all chain instances */
	private static final LinkedBlockingQueue<ChainRunnerWhoisQuery> inQueue =
			new LinkedBlockingQueue<>();
			
	private static Boolean isConsumerRunnig = false;
	
	public ChainRunnerWhoisQuery() {
		setChainName("Whois Lookup");
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("asNum"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("bgpPrefix"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("cc"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("asOnwer"));
		
		if (isConsumerRunnig == false) {
			firstChainInit();
		}
	}

	/* the first ChainRunnerWhoisQuery created of this type will execute the consumer
	 * thread, which creates the actual queries coming from all other ChainRunnerWhoisQueries
	 */
	private void firstChainInit() {
		Factory.getFactory().getExecutorForPerodicRunnableTask(new ChainRunnerWhoisQueryWorker(), 
															   1, 1, TimeUnit.SECONDS);
		isConsumerRunnig = true;
	}
	
	@Override
	public void run() {
		inQueue.add(this);
	}

	/* internal private class for worker thread; returns a Future reference
	 * to the object called so it can be used to check return value */
	private class ChainRunnerWhoisQueryWorker implements Runnable{	
		
		Map<String,ArrayList<ChainRunnerWhoisQuery>> pendingChainsMap = 
				new HashMap<>();
		Set<String> whoIsQueries = new HashSet<>();
				
		@Override
		public void run() {		
			try {
				ChainRunnerWhoisQuery chainRunner = null;
				ArrayList<ChainRunnerWhoisQuery> pendingChainRunners = null;
				setStatus(ProcessingChain.chainStatus.OK);
					
				while (inQueue.isEmpty() == false && whoIsQueries.size() < MAX_QUERIES_IN_REQUEST) {
					chainRunner = inQueue.take();
					DomainToAnalyze domainToAnalyze = chainRunner.domainToAnalyze;
						
					logger.info("popped: " + domainToAnalyze.getDomainName());
						
					Record[] records = (Record[]) domainToAnalyze.getPropertiesMap().get("domainRecords");
					if (records == null) {
						chainRunner.flush();
						continue;
					}
					
					/* take the first entry. In case a single IP address can
					 * match more than one domain, we keep it in a list
					 */
					pendingChainRunners = pendingChainsMap.get(records[0].rdataToString());
					if (pendingChainRunners == null) {
						pendingChainRunners = new ArrayList<>();
					}
					pendingChainRunners.add(chainRunner);
					
					pendingChainsMap.put(records[0].rdataToString(), pendingChainRunners);
					whoIsQueries.add(records[0].rdataToString());
				}
				if (whoIsQueries.size() > 0) {
					createWhoisQuery(pendingChainsMap, whoIsQueries);
				}
			} catch (Exception e) {
					logger.error("caught exception ", e);
					setStatus(ProcessingChain.chainStatus.ERROR);
			} finally {
				for (ArrayList<ChainRunnerWhoisQuery> chainRunners : pendingChainsMap.values()) {
					for (ChainRunnerWhoisQuery chainRunner : chainRunners) {
						chainRunner.setStatus(getStatus());
						chainRunner.flush();
					}
				}
				pendingChainsMap.clear();
				whoIsQueries.clear();
			}
		}
	
		private void createWhoisQuery(
				Map<String, ArrayList<ChainRunnerWhoisQuery>> pendingChainsMap,
				Set<String> whoIsQueries) throws UnknownHostException, IOException {
				ArrayList<ChainRunnerWhoisQuery> pendingChainRunners = null;
				
				logger.info("creating a WhoIsQuery for " + whoIsQueries.size() + " entries");
				
				ArrayList<String> whoisLookupResponses = 
						whoisLookup.bulkLookup(WHOIS_SERVER_NAME, whoIsQueries);
				ArrayList<WhoisQueryResults> whoisQueryResults =
						whoisLookup.parseWhoisResponse(whoisLookupResponses);
				
				for (WhoisQueryResults results : whoisQueryResults) {
					pendingChainRunners = pendingChainsMap.get(results.getIpAddr());
					
					/* update all chains relevant to this whois query */
					
					for (ChainRunnerWhoisQuery chainRunner : pendingChainRunners) {
						Map<String, Feature> featuresMap = 
								chainRunner.domainToAnalyze.getFeaturesMap();
						
						featuresMap.get("asNum").setValue(Integer.parseInt(results.getAsNum()));
						featuresMap.get("bgpPrefix").setValue(results.getBgpPrefix());
						featuresMap.get("cc").setValue(results.getCc());
						featuresMap.get("asOnwer").setValue(results.getAsOnwer());
					}
				}
			}
		}
}
