package il.ac.mta.bi.dmd.common;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
	private Integer totalUpdates = 0;
	private String modelOutputDir;
	private Integer classifierCode;
	private String nickName; 
	private static final Integer CLASS_BUILD_RATIO = 500;
	
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
		eval.evaluateModel(classifier, dataSet);
	}
	
	public void updateClassifier(Instance instanceData) throws Exception {
		dataSet.add(instanceData);
		totalUpdates ++;
		
		/* rebuild/update the classifier */
		if (classifier instanceof UpdateableClassifier) {
			logger.info("updating classifier...");
			UpdateableClassifier updateableClassifier =
					(UpdateableClassifier) this.classifier;
			updateableClassifier.updateClassifier(instanceData);
		} else { 
			if (totalUpdates % CLASS_BUILD_RATIO == 0) {
				logger.info("rebuilding classifier...");
				classifier.buildClassifier(dataSet);
			}
		}
	}
	
	public double classifyInstanceShort(Instance instanceData) throws Exception {
		return classifier.classifyInstance(instanceData);
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
		return totalUpdates;
	}
	
	public void deSerialize() throws ClassNotFoundException, IOException {
		 // deserialize model
		String modelFullPathName = classifierCode + "_" + nickName + ".model";
		modelFullPathName = modelOutputDir + "\\" + modelFullPathName;
				
		java.io.File fileModel = new java.io.File(modelFullPathName);
		if(fileModel.exists()) {
			logger.info("reading model from " + modelFullPathName);
			logger.info("deserializing model...");
			
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileModel));
			classifier = (Classifier) ois.readObject();
			ois.close();
			
			logger.info("model deserialized successfully");
		}
		
		String arffFullPathName = classifierCode + "_" + nickName + ".arff";
		arffFullPathName = modelOutputDir + "\\" + arffFullPathName;
		
		java.io.File fileArff = new java.io.File(arffFullPathName);
		if(fileArff.exists()) {
			logger.info("reading arff data from " + arffFullPathName);
			logger.info("reading arff data...");
			
			dataSet = new Instances(new FileReader(fileArff));
			
			logger.info("arff data read successfully, num of instances read=" + 
					dataSet.numInstances());
			
			dataSet.setClassIndex(dataSet.numAttributes()-1);
		}
		
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
		
		String arffFullPathName = classifierCode + "_" + nickName + ".arff";
		arffFullPathName = modelOutputDir + "\\" + arffFullPathName;
		
		logger.info("wariting arff data"); 
		logger.info("writing Classifier arff data to " +
				arffFullPathName);
		Writer writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(arffFullPathName), "utf-8"));
		writer.write(dataSet.toString());
		writer.close();
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
