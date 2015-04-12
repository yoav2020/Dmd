package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.ClassifierWrapper;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import weka.core.Instance;

/**
 * The classification chain, based on the classifier created in the previous chains
 */

public class ChainRunnerClassify extends ProcessChain {
	private static Logger 	logger = Logger.getLogger(ChainRunnerClassify.class);
	
	public ChainRunnerClassify() {
		setChainName("Classifier Runner");
	}

	@Override
	public void run() {
		try {
			if (domainToAnalyze.getClassification() == Classification.UNKNOWN) {
				logger.info("classifying domain " + domainToAnalyze.getDomainName());
				ClassifierWrapper classifierWrapper = 
						(ClassifierWrapper) domainToAnalyze.getPropertiesMap().get("myClassifier");
				Instance instanceData = 
						(Instance) domainToAnalyze.getPropertiesMap().get("instanceData");
				double[] result = classifierWrapper.classifyInstance(instanceData);
				
				domainToAnalyze.setMaliciousChance(result[0]);
				domainToAnalyze.setBenignChance(result[1]);
				domainToAnalyze.setClassification(classifierWrapper.classifyInstanceShort(instanceData));
				
				logClassification(classifierWrapper, result);
			} else {
				logger.info("domain type is known, nothing to do");
			}
		} catch (Exception e) {
			logger.error("caught exception ", e);
			setStatus(ProcessingChain.chainStatus.ERROR);
		}
		
		flush();
	}

	private void logClassification(ClassifierWrapper classifierWrapper,
			double[] result) {

		logger.info("classifier name=" + classifierWrapper.getNickName());
		logger.info("malicious chance=" + result[0]);
		logger.info("benign chance=" + result[1]);
		logger.info("domain is " + domainToAnalyze.getClassification());
		
		if (logger.isEnabledFor(Level.INFO)) {
			System.out.println("---------------");	
			System.out.println(domainToAnalyze.toStringFull());
			System.out.println("---------------");	
		}
	}
}
