package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.net.InternetDomainName;

public class ChainRunnerTLD extends ProcessChain {

    static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
    private String FEATURE_NAME = "TopLevelDomain";

    public ChainRunnerTLD() {
        setChainName("TopLevelDomain checker");
        chainFeaturesList.add(Factory.getFactory().getStringFeature(FEATURE_NAME));
    }

    @Override
    public void run() {
    	try {
	        logger.info("Getting " + domainToAnalyze.getDomainName() + " top level domain.");
	        // Get the TLD
	        String strTld = InternetDomainName.from(domainToAnalyze.getDomainName()).publicSuffix().name();
	        logger.info("Checking domain " + domainToAnalyze.getDomainName() + " publicSufix is : " + strTld);
	        
	        // Update the features map
	        Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
	        Feature feature = featuresMap.get(FEATURE_NAME);
	        feature.setValue(strTld);
	        
	        // Exit
	        logger.info("Finished checking domain. TLD is " + strTld);
    	} catch (Exception e) {
			logger.error("caught exception ", e);
			setStatus(ProcessingChain.chainStatus.ERROR);
    	}
	    flush();
    }

}
