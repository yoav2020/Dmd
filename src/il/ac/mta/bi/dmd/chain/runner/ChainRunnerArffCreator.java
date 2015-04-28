package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;

import java.util.Map;

import org.apache.log4j.Logger;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * A classifier requires an arff file to function. This file contains the features (attributes)
 * as well as their values, all stored in the same file separated by headers. This chain creates 
 * the arff file out of the domain features, however it doesn't fill up any features but only 
 * properties. These are:
 * 
 * "fvWekaAttributes" - the arff structures attributes, without the values;
 * "fvWekaAttributesHash" - the hash value of the arff attribute (actually, the hashcode is called
 * on the stringed version of the arff (the arff itself is a text format);
 * "instanceData" - the feature values for the classification. These match the attributes in the 
 * arff file;
 * 
 * More info about the arff file in general can be found here:
 * http://weka.wikispaces.com/ARFF+%28stable+version%29
 */

public class ChainRunnerArffCreator extends ProcessChain {
	private static Logger logger = Logger.getLogger(ChainRunnerArffCreator.class);

	private FastVector fvWekaAttributes;
	private Integer fvWekaAttributesHash;
	private Instances dataSet;
	private Instance instanceData;
	
	public ChainRunnerArffCreator() {
		setChainName("Arff Consturctor");
	}

	@Override
	public void run() {
		try {
			createFvWekaAttributes();
			createDataInstance();
			domainToAnalyze.getPropertiesMap().put("fvWekaAttributes", fvWekaAttributes);
			domainToAnalyze.getPropertiesMap().put("fvWekaAttributesHash", fvWekaAttributesHash);
			domainToAnalyze.getPropertiesMap().put("instanceData", instanceData);
			domainToAnalyze.getPropertiesMap().put("dataSet", dataSet);
		} catch (Exception e) {
			logger.error("caught exception ", e);
			setStatus(ProcessingChain.chainStatus.ERROR);
		}
		
		flush();
	}
	
	/* convert all data to numeric */
	private void createDataInstance() {
		Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
		Instance instanceData = new SparseInstance(featuresMap.size());
		Instances dataSet = new Instances("Domain class relation", fvWekaAttributes, 0);
		dataSet.setClassIndex(dataSet.numAttributes()-1);
		int attIndex = 0;
		instanceData.setDataset(dataSet);
		
		for (Feature feature : featuresMap.values()) {
			switch (feature.getType()) {
			case INTEGER:
				if (feature.getValue() != null) {
					instanceData.setValue(attIndex, (Integer)feature.getValue());
				} else {
					instanceData.setValue(attIndex, 0);
				}
				break;
			case NOMINAL:
			case STRING:
				if (feature.getValue() != null) {
					instanceData.setValue(attIndex, ((String)feature.getValue()).hashCode());
				} else {
					instanceData.setValue(attIndex, "".hashCode());
				}
				break;
			default:
				logger.warn("attribute not supported");
			}
			logger.info(feature.getName() + ": " + feature.getValue() + " ; internal val=" +
						instanceData.value(attIndex));
			attIndex ++;
		}
		
		/* add classification for known domains only */
		if (domainToAnalyze.getClassification() != Classification.UNKNOWN) {
			instanceData.setValue(attIndex, domainToAnalyze.getClassification().toString());
		}
		
		logger.info("classification " + ": " + domainToAnalyze.getClassification().toString());

		this.dataSet = dataSet;
		this.instanceData = instanceData;
		
		logger.info("printable arff: " + this.instanceData.toString());
		logger.info("data instance created successfully!");
	}
	
	private void createFvWekaAttributes() {
		Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
		FastVector fvWekaAttributes = new FastVector();
		StringBuilder fvWekaAtrributeString = new StringBuilder();
		
		for (Feature feature : featuresMap.values()) {
			logger.info("adding attribute to model: " + feature.toAttribute().toString());
			fvWekaAttributes.addElement(feature.toAttribute());
			fvWekaAtrributeString.append(feature.toAttribute().toString());
		}
		/* add the class attribute last */
		logger.info("adding attribute to model: " + domainToAnalyze.classToAttribute().toString());
		
		fvWekaAttributes.addElement(domainToAnalyze.classToAttribute());
		fvWekaAtrributeString.append(domainToAnalyze.classToAttribute().toString());
	   
		fvWekaAttributes.trimToSize();
	   
		this.fvWekaAttributes = fvWekaAttributes;
		this.fvWekaAttributesHash = fvWekaAtrributeString.toString().hashCode();
	
		logger.info("created arff successfully, hash=" + fvWekaAttributesHash);
	}
}
