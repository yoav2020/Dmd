package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.infra.Factory;
import il.ac.mta.bi.dmd.lookup.WhoisLookup;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class ChainRunnerWhoisQuery extends ProcessChain {
	static Logger 					logger = Logger.getLogger(ChainRunnerDnsLookup.class);
	
	private static final WhoisLookup whoisLookup = new WhoisLookup();
	private static final String whoisServer = "whois.cymru.com";
	
	public ChainRunnerWhoisQuery() {
		setChainName("Whois Lookup");
		chainFeaturesList.add(Factory.getFactory().getIntegerFeature("asNum"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("ipAddr"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("bgpPrefix"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("cc"));
		chainFeaturesList.add(Factory.getFactory().getStringFeature("asOnwer"));
	}
	
	@Override
	public void run() {
		Factory.getFactory().getExecutorForCallableTask(new ChainRunnerWhoisQueryWorker());
	}

	/* internal private class for worker thread; returns a Future reference
	 * to the object called so it can be used to check return value */
	private class ChainRunnerWhoisQueryWorker implements Callable<Object>{		
		@Override
		public Object call() throws InterruptedException {
			flush();
			
			return this;
		}
	}
}
