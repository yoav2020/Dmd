package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.ClassifierWrapper;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This chain builds the classifier for a given arff. It also trains the classifier and
 * tests it, periodically. It fills the property ""myClassifier" - the classifier instance
 * to be used for classifying the domain
 */

public class ChainRunnerClassifierBuilder extends ProcessChain {
	static Logger 	logger = Logger.getLogger(ChainRunnerClassifierBuilder.class);
	
	private static Map<Integer,ClassifierWrapper> classifierMap = new HashMap<>();
	private static final Integer CLASS_TEST_RATIO = 4;
	
	public ChainRunnerClassifierBuilder() {
		setChainName("Classifier Builder Runner");
	}

	@Override
	public void run() {
		try {
			Integer fvWekaAttributesHash = 
					(Integer) domainToAnalyze.getPropertiesMap().get("fvWekaAttributesHash");

			Instance instanceData = 
					(Instance) domainToAnalyze.getPropertiesMap().get("instanceData");
			
			ClassifierWrapper classifierWrapper = classifierMap.get(fvWekaAttributesHash);
			if (classifierWrapper == null) {
				classifierWrapper = generateClassifier();
				classifierMap.put(fvWekaAttributesHash, classifierWrapper);
			}		
			
			/* if we are certain of the domain classification - use it for
			 * training or evaluation testing
			 */
			
			if (domainToAnalyze.getClassification() != Classification.UNKNOWN) {
				maintainModel(instanceData, classifierWrapper);
			} 
			
			/* insert the classifier to the map */
			
			domainToAnalyze.getPropertiesMap().put("myClassifier", classifierWrapper);
		} catch (Exception e) {
			logger.error("caught exception ", e);
			setStatus(ProcessingChain.chainStatus.ERROR);
		}
		
		flush();
	}

	private void maintainModel(Instance instanceData,
			ClassifierWrapper classifierWrapper) throws Exception {
		if (classifierWrapper.getTotalClassifications() % CLASS_TEST_RATIO == 
				CLASS_TEST_RATIO-1) {
			logger.info("testing classifier");
			classifierWrapper.evaluateModel(instanceData);
			logger.info(classifierWrapper.getStats());
		} else {
			logger.info("training classifier");
			classifierWrapper.updateClassifier(instanceData);
		}
	}

	private ClassifierWrapper generateClassifier() throws Exception {
		Instances dataSet = (Instances) domainToAnalyze.getPropertiesMap().get("dataSet");
		ClassifierWrapper classifierWrapper = new ClassifierWrapper();

		classifierWrapper.buildClassifier(dataSet);
		classifierWrapper.setDataSet(dataSet);
		classifierWrapper.setEval(new Evaluation(dataSet));
		
		logger.info("classifier created successfully!");
		
		return classifierWrapper;
	}

}
