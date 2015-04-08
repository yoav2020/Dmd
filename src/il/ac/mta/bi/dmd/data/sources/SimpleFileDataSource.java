package il.ac.mta.bi.dmd.data.sources;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.infra.Factory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

public class SimpleFileDataSource extends DataSource {
	static Logger logger = Logger.getLogger(SimpleFileDataSource.class);
	
	private String fileName;
	private boolean fileOpened = false;

	public SimpleFileDataSource() {
		setSourceName("SimpleFileDataSource");
	}
	
	@Override
	public void run() {
		if(fileOpened == true) {
			return;
		}
		
		logger.info("opening "+ "\"" + fileName + "\"");
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileName));	
			String line;
			String domainName;
			String classification;
		
			while ((line = reader.readLine()) != null) {
				domainName = line.split(" ")[0];
				if (line.split(" ").length == 2) {
					classification = line.split(" ")[1];
				} else {
					classification = "UNKNOWN";
				}
				
				DomainToAnalyze domainToAnalyze=
				Factory.getFactory().getDomainToAnalyze(domainName, classification);
				getDispatchQueue().add(domainToAnalyze);
			}
		} catch (IOException e) {
			logger.error("caught exception", e);
		}
		
		logger.info("successfully opened " + "\"" + fileName + "\"");
		fileOpened = true;
	}
	
	public void reset() {
		fileOpened = false;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
