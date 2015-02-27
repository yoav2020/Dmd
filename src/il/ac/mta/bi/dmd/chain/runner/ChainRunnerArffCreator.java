package il.ac.mta.bi.dmd.chain.runner;

import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class ChainRunnerArffCreator extends ProcessChain {
	private static Logger logger = Logger.getLogger(ChainRunnerArffCreator.class);

	private FastVector fvWekaAttributes;
	private Instance instanceData;
	
	public ChainRunnerArffCreator() {
		setChainName("Arff Consturctor");
	}

	@Override
	public void run() {
		createFvWekaAttributes();
		createInstance();
		flush();
	}
	
	private void createInstance() {
		Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
		Instance instanceData = new SparseInstance(featuresMap.size());
		int i = 0;
		
		for (Feature feature : featuresMap.values()) {
			switch (feature.getType()) {
			case INTEGER:
				instanceData.setValue((Attribute)fvWekaAttributes.elementAt(i), 
						(Integer)feature.getValue());
				break;
			case NOMINAL: /* not supported yet */
				break;
			case STRING:
			default:
				instanceData.setValue((Attribute)fvWekaAttributes.elementAt(i), 
						(String)feature.getValue());
				break;
			}
			logger.info(feature.getName() + ": " + feature.getValue());
			i ++;
		}
		
		this.instanceData = instanceData;
	}
	
	private void createFvWekaAttributes() {
		Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
		FastVector fvWekaAttributes = new FastVector();
		
		for (Feature feature : featuresMap.values()) {
			fvWekaAttributes.addElement(feature.toAttribute());
			logger.info("adding attribute to model: " + feature.toAttribute().toString());
		}
		
	   FastVector fvClassVal = new FastVector();
	   fvClassVal.addElement("malicious");
	   fvClassVal.addElement("benign");
	   Attribute ClassAttribute = new Attribute("domainClassification", fvClassVal);
	   fvWekaAttributes.addElement(ClassAttribute);
	   
	   this.fvWekaAttributes = fvWekaAttributes;
	
	   logger.info("created Attribute-Relation File Format (arff) successfully!");
	}
}
