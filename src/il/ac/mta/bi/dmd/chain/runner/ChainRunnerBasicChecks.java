package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This Chain does some basic checks
 *
 * @author Mike
 */
public class ChainRunnerBasicChecks extends ProcessChain {

    static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
    private String DOMAIN_LENGTH = "DomainLength";
    private String NUMBER_DOTS = "NumberOfDots";

    public ChainRunnerBasicChecks() {
        setChainName("Basic checks runner");
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(DOMAIN_LENGTH));
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(NUMBER_DOTS));
    }

    @Override
    public void run() {
    	try {
	        logger.info("Getting " + domainToAnalyze.getDomainName() + " basic checks.");
	        String strDomain = domainToAnalyze.getDomainName();
	        Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
	        String TLD = (String) featuresMap.get("TopLevelDomain").getValue();
	
	        // Getting length of the domain; make sure to remove TLD and it's
	        // preceding dot.
	        int nLength = strDomain.length() - TLD.length() - 1;	
	        int nDotsCount = StringUtils.countMatches(strDomain, ".");
	
	        // Update the map
	        featuresMap.get(DOMAIN_LENGTH).setValue(nLength);
	        featuresMap.get(NUMBER_DOTS).setValue(nDotsCount);
	        
	        logger.info("Domain length : " + nLength);
	        logger.info("Number of dots in domain : " + nDotsCount);
	
	        logger.info("Finished checking domain. ");
    	} catch (Exception e) {
    		logger.error("caught exception ", e);
    		setStatus(ProcessingChain.chainStatus.ERROR);
    	}
        flush();
    }

}
