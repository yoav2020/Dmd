package il.ac.mta.bi.dmd.common;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;

public class ClassifierWrapper {
	private NaiveBayesUpdateable updateableClassifier;
	private Instances dataSet;
	private Evaluation eval;
	private Integer totalClassifications = 0;
	
	public String getStats() {
		return eval.toSummaryString();
	}
	
	public ClassifierWrapper() {
		updateableClassifier = new NaiveBayesUpdateable();
	}
	
	public void buildClassifier(Instances dataSet) throws Exception {
		updateableClassifier.buildClassifier(dataSet);
	}
	
	public void evaluateModel(Instance instanceData) throws Exception {
		totalClassifications ++;
		
		dataSet.add(instanceData);
		eval.evaluateModel(updateableClassifier, dataSet);
	}
	
	public void updateClassifier(Instance instanceData) throws Exception {
		totalClassifications ++;
		
		instanceData.setDataset(dataSet);
		updateableClassifier.updateClassifier(instanceData);
	}
	
	public double[] classifyInstance(Instance instanceData) throws Exception {
		totalClassifications ++;
		
		instanceData.setDataset(dataSet);
		double[] result = updateableClassifier.distributionForInstance(instanceData);

		return result;
	}
	
	public Instances getDataSet() {
		return dataSet;
	}
	
	public void setDataSet(Instances dataSet) {
		this.dataSet = dataSet;
	}
	
	public void setEval(Evaluation eval) {
		this.eval = eval;
	}

	public Integer getTotalClassifications() {
		return totalClassifications;
	}
}
