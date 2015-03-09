package il.ac.mta.bi.dmd.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;

/* A wrapper for the classifier, of type NaiveBayesUpdateable. From wikipeida 
 * (http://en.wikipedia.org/wiki/Naive_Bayes_classifier):
 * 
 * Naive Bayes is a simple technique for constructing classifiers: models that assign class labels to 
 * problem instances, represented as vectors of feature values, where the class labels are drawn from 
 * some finite set. It is not a single algorithm for training such classifiers, but a family of algorithms
 * based on a common principle: all naive Bayes classifiers assumes that the value of a particular feature
 * is independent of the value of any other feature, given the class variable. For example, a fruit may
 * be considered to be an apple if it is red, round, and about 3" in diameter. A naive Bayes classifier
 * considers each of these features to contribute independently to the probability that this fruit is
 * an apple, regardless of any possible correlations between the color, roundness and diameter features.
 */

public class ClassifierWrapper {
	private Classifier classifier;
	private Instances dataSet;
	private Evaluation eval;
	private Integer totalClassifications = 0;
	private String modelOutputDir;
	private Integer classifierCode;
	private String nickName; 
	private static final Integer CLASS_BUILD_RATIO = 5;
	
	private static Logger 	logger = Logger.getLogger(ClassifierWrapper.class);
	
	public String getStats() {
		return eval.toSummaryString();
	}
	
	public ClassifierWrapper(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public void buildClassifier(Instances dataSet) throws Exception {
		classifier.buildClassifier(dataSet);
	}
	
	public void evaluateModel(Instance instanceData) throws Exception {
		totalClassifications ++;
		
		dataSet.add(instanceData);
		eval.evaluateModel(classifier, dataSet);
	}
	
	public void updateClassifier(Instance instanceData) throws Exception {
		totalClassifications ++;
		
		if (classifier instanceof UpdateableClassifier) {
			UpdateableClassifier updateableClassifier =
					(UpdateableClassifier) this.classifier;
			updateableClassifier.updateClassifier(instanceData);
		} else { 
			if (totalClassifications % CLASS_BUILD_RATIO == 0) {
				dataSet.add(instanceData);
				classifier.buildClassifier(dataSet);
			}
		}
	}
	
	public double[] classifyInstance(Instance instanceData) throws Exception {
		totalClassifications ++;
		
		double[] result = classifier.distributionForInstance(instanceData);
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
	
	public void deSerialize() throws ClassNotFoundException, IOException {
		 // deserialize model
		String modelFullPathName = classifierCode + "_" + nickName + ".model";
		modelFullPathName = modelOutputDir + "\\" + modelFullPathName;
		
		logger.info("reading model from " + modelFullPathName);
		
		java.io.File fileModel = new java.io.File(modelFullPathName);
		
		if(fileModel.exists() == false) {
			return;
		}
		
		logger.info("deserializing model");
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileModel));
		classifier = (NaiveBayesUpdateable) ois.readObject();
		ois.close();
	}
	
	public void serialize() throws IOException {
		// serialize model
		String modelFullPathName = classifierCode + "_" + nickName + ".model";
		modelFullPathName = modelOutputDir + "\\" + modelFullPathName;
		
		logger.info("serializing model");
		
		logger.info("writing model to " + modelFullPathName);
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFullPathName));
		oos.writeObject(classifier);
		oos.flush();
		oos.close();
	}

	public String getModelOutputDir() {
		return modelOutputDir;
	}

	public void setModelOutputDir(String modelOutputDir) {
		this.modelOutputDir = modelOutputDir;
	}

	public Integer getClassifierCode() {
		return classifierCode;
	}

	public void setClassifierCode(Integer classifierCode) {
		this.classifierCode = classifierCode;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
}
