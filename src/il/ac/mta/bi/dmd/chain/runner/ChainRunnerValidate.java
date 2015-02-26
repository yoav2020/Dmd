package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.log4j.Logger;

/**
 * This chain runner validates the domain name, and confirm it is legal and stands for standards. 
 * It doesn't fill any features in the features map, but instead fails the chain to end the 
 * domain handling prematurely
 */

public class ChainRunnerValidate extends ProcessChain {
	static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
	
	public ChainRunnerValidate() {
		setChainName("Domain Validator");
	}
	
	@Override
	public void run() {
		if (!DomainValidator.getInstance().isValid(domainToAnalyze.getDomainName())) {
			logger.info("domain is INVALID");
			setStatus(ProcessingChain.chainStatus.ERROR);
		}
		else {
			logger.info("domain is VALID");
		}
		
		flush();
	}
}
