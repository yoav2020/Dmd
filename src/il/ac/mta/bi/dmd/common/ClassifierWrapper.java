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
import weka.core.Instance;
import weka.core.Instances;

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
				
		java.io.File fileModel = new java.io.File(modelFullPathName);
		
		if(fileModel.exists() == false) {
			return;
		}
		
		logger.info("reading model from " + modelFullPathName);
		logger.info("deserializing model...");
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileModel));
		classifier = (Classifier) ois.readObject();
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
