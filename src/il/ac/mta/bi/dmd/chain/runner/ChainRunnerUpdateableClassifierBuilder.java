package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.ClassifierWrapper;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This chain builds the classifier for a given arff. It also trains the classifier and
 * tests it, periodically. It fills the property ""myClassifier" - the classifier instance
 * to be used for classifying the domain
 */

public class ChainRunnerUpdateableClassifierBuilder extends ProcessChain {
	static Logger 	logger = Logger.getLogger(ChainRunnerUpdateableClassifierBuilder.class);
	
	private static Map<Integer,ClassifierWrapper> classifierMap = new HashMap<>();
	private static final Integer SERIALIZATION_RATIO = 128;
	private static final String MODEL_OUT_DIR = "data";
	
	public ChainRunnerUpdateableClassifierBuilder() {
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
				classifierWrapper = getClassifier();
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
		
		logger.info("training classifier");
		classifierWrapper.updateClassifier(instanceData);
		
		/* model serialization */
		
		if (classifierWrapper.getTotalClassifications() % SERIALIZATION_RATIO == 0) {
			try {
				classifierWrapper.serialize();
				logger.info("successfully saved the model");
			} catch (IOException e) {
				logger.error("failed to serialize model", e);
			}
		}
	}

	private ClassifierWrapper getClassifier() throws Exception {
		Instances dataSet = (Instances) domainToAnalyze.getPropertiesMap().get("dataSet");
		Integer classifierCode = (Integer) domainToAnalyze.getPropertiesMap().get("fvWekaAttributesHash");

		ClassifierWrapper classifierWrapper = generateClassifier(dataSet, classifierCode);
		try {
			classifierWrapper.deSerialize();
		} catch (Exception e) {
			logger.error("failed to deserialize model", e);
		}
		
		logger.info("classifier created successfully!");
		
		return classifierWrapper;
	}

	private ClassifierWrapper generateClassifier(Instances dataSet,
			Integer classifierCode) throws Exception {
		ClassifierWrapper classifierWrapper = 
				new ClassifierWrapper(new NaiveBayesUpdateable());
		classifierWrapper.buildClassifier(dataSet);
		classifierWrapper.setDataSet(dataSet);
		classifierWrapper.setEval(new Evaluation(dataSet));
		classifierWrapper.setModelOutputDir(MODEL_OUT_DIR);
		classifierWrapper.setClassifierCode(classifierCode);
		classifierWrapper.setNickName("updateable_classifier");
		return classifierWrapper;
	}

}
