package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;

import org.apache.log4j.Logger;

public class ChainRunnerWeka extends ProcessChain {
	static Logger 	logger = Logger.getLogger(ChainRunnerWeka.class);

	public ChainRunnerWeka() {
		setChainName("Weka");
	}

	@Override
	public void run() {
		for (Feature entry : domainToAnalyze.getFeaturesMap().values()) {
			logger.info("Feature is: " + entry.getName());
		}		
		flush();
	}

}
