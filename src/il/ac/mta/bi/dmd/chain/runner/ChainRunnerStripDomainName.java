package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * This chain runner returns the domain name by stripping all prefixes such as
 * www, ftp, etc. The domain name is replaced, and the old name is stored in 
 * the properties map
 */

public class ChainRunnerStripDomainName extends ProcessChain {
	private static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
	private static String stripRegex = "^www[0-9]?.|^ftp.|^smtp.";
	
	public ChainRunnerStripDomainName() {
		setChainName("Domain Name Strip");
	}
	
	@Override
	public void run() {
		try {
			logger.info("domain before strip=" + domainToAnalyze.getDomainName());
			
			String domain = domainToAnalyze.getDomainName();
			String domainUrl = domainToAnalyze.getDomainName();
			
			/* add an http prefix to create a url object */
			if (domainUrl.startsWith("http") == false) {
				domainUrl = "http://" + domain;
		    }
			
			URL url = new URL(domainUrl);
			String strippedDomainName = url.getHost().replaceAll(stripRegex, "");
			
			if (domain.equals(strippedDomainName) == false) {
				domainToAnalyze.getPropertiesMap().put("beforeStrip", domainToAnalyze.getDomainName());
				domainToAnalyze.setDomainName(strippedDomainName);
			}
			
			logger.info("domain after strip=" + domainToAnalyze.getDomainName());
		} catch (MalformedURLException e) {
			logger.error("caught exception ", e);
			setStatus(ProcessingChain.chainStatus.ERROR);
		}
		
		flush();
	}
}
