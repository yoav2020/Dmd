package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.net.InternetDomainName;

public class ChainRunnerTLD extends ProcessChain {

    static Logger 	logger = Logger.getLogger(ChainRunnerDnsLookup.class);
    private String 	FEATURE_NAME = "TopLevelDomain";

    public ChainRunnerTLD() {
        setChainName("TopLevelDomain checker");
        chainFeaturesList.add(Factory.getFactory().getStringFeature(FEATURE_NAME));
    }

    @Override
    public void run() {
        Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
        Feature feature = featuresMap.get(FEATURE_NAME);
        String strTld = null;
        
    	try {
	        logger.info("Getting " + domainToAnalyze.getDomainName() + " top level domain.");
	        
	        // Get the TLD. If the TLD is not common, assume it is the last dot followed
	        // string
	        if (InternetDomainName.from(domainToAnalyze.getDomainName()).publicSuffix() == null) {
	    		logger.info("found a non common TLD");
	    		String [] domSplit = domainToAnalyze.getDomainName().split("\\.");
	    		strTld = domSplit[domSplit.length-1];
	        } else {
	        	strTld = InternetDomainName.from(domainToAnalyze.getDomainName()).publicSuffix().name();
	        }

            feature.setValue(strTld);
            logger.info("Finished checking domain. TLD= " + strTld);
            
    	} catch (Exception e) {
			logger.error("caught exception ", e);
			setStatus(ProcessingChain.chainStatus.ERROR);
    	}
		flush();
    }

}
