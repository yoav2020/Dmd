package il.ac.mta.bi.dmd.data.sources;

import il.ac.mta.bi.dmd.common.DataSource;

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
		
			while ((line = reader.readLine()) != null) {
				addDomainFromSource(line);
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
