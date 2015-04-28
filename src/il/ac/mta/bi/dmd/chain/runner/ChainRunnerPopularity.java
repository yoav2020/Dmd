package il.ac.mta.bi.dmd.chain.runner;

import static java.lang.Integer.parseInt;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

/**
 * This class checks the rank of a given domain. It downloads a csv from alexa
 * website , then unzips it and reads the ranks of the top 1000000 sites. later
 * there is a hash table that is built to support the the fetching of the ranks
 */
public class ChainRunnerPopularity extends ProcessChain {

    static Logger 						logger = Logger.getLogger(ChainRunnerPopularity.class);
    private String 						FILE_DIR = "data";
    private String 						FILE_NAME = "top-1m-csvsites.zip";
    private String 						FILE_NAME_OUTPUT = "top-1m.csv";
    private String 						FILE_URL = "http://s3.amazonaws.com/alexa-static/top-1m.csv.zip";
    private static Map<String, Integer> mSitePopulatiry = new HashMap<String, Integer>();
    private int 						HASH_SIZE = 100000;
    private String 						FEATURE_NAME = "domainRank";
    public boolean 						showPos = false;
    private static boolean 				downloading = false;
    private static Date 				lastMod;

    public ChainRunnerPopularity() {
        setChainName("Popularity checker");
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(FEATURE_NAME));
    }
    
    private class AlexaDownloader implements Runnable {
    	
    	 /**
         * This function downloads the zip file from alexa website
         */
        private void DownloadCSV() {
            URL urlFile = null;
            URLConnection urlCon = null;

            try {
                urlFile = new URL(FILE_URL);
                urlCon = urlFile.openConnection();
                urlCon.connect();
                int lFileLength = urlCon.getContentLength();
                File flsSave = new File(FILE_NAME);
                BufferedInputStream bufferInpStream = new BufferedInputStream(urlCon.getInputStream());
                FileOutputStream bufferOutStream = new FileOutputStream(flsSave.getName());

                byte bData[] = new byte[1024];
                int lTotalData = 0;
                int nByteCount = 0;
                int nTotalDownloadSize = (int) lFileLength / 1024;
                float nProgress = 0;
                float fLast = -1;
                logger.info("Dowinloading " + FILE_NAME + " Total download size : " + nTotalDownloadSize + " KB");

                // Read 1024 bytes everytime 
                while ((nByteCount = bufferInpStream.read(bData)) != -1) {
                    bufferOutStream.write(bData, 0, nByteCount);
                    lTotalData += nByteCount;
                    nProgress = (((float) lTotalData * 100 / lFileLength));
                    nProgress = Math.round(nProgress);
                    if (nProgress % 10 == 0 && fLast < nProgress) {
                        logger.info("Total progress: %" + nProgress);
                        fLast = nProgress;
                    }
                }
                logger.info("Download finished.");

                bufferOutStream.flush();
                bufferOutStream.close();

                // Set the last modified on the file as the current date
                Date now = Calendar.getInstance().getTime();
                flsSave.setLastModified(now.getTime());

            } catch (MalformedURLException ex) {
                logger.error("caught exception ", ex);
            } catch (IOException ex) {
                logger.error("caught exception ", ex);
            }
        }

        /**
         * This function unzips the given zip file
         * @throws IOException 
         */
        private void UnzipCSV() throws IOException {

            String zipFile = FILE_NAME;
            String outputFolder = FILE_DIR;

            byte[] buffer = new byte[1024];

            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            logger.info("Unziping " + FILE_NAME);
            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                logger.info("Unziping : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos;

                fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            logger.info(FILE_NAME + " Unziped.");
        }

		@Override
		public void run() {
			try {
	            // Download the new zip
	            DownloadCSV();
	            
	            //Unzip the zip
	            UnzipCSV();
	            
	            // Load the new HashMap
	            switchHashMap();
	            
	            // update last modified time
	            updateLastModifiedTime(new File(FILE_NAME));
			} catch (Exception e) {
				logger.error("caught exception ", e);
			} finally {
	            // even if download failed,
				// allow the file to be downloaded again
				ChainRunnerPopularity.downloading = false;
			}
		}
    	
    }

    @Override
    public void run() {
        logger.info("Getting " + domainToAnalyze.getDomainName() + " rank.");

        try {
        	
        	/* first chain run */
        	
        	if (mSitePopulatiry == null || mSitePopulatiry.isEmpty()) {
    	        File flCheck = new File(FILE_NAME);
    	        
    	        /* if no file exists - download the Alexa ranking file on the
    	         * main thread to block other domains 
    	         */
    	        if (flCheck.exists() == false) {
    	        	logger.info("popularity file not found, downloading (blocking)");
        	        AlexaDownloader alexDownloader = new AlexaDownloader();
        	        alexDownloader.run();
    	        }
    	        
    	        LoadHashMap();
    	        
    	        updateLastModifiedTime(new File(FILE_NAME));
        	}
        	
            // Is there a new version needed?
        	// Don't start downloading if downloading already
        	// started. Download is performed on sepearate thread.
        	
            if (ChainRunnerPopularity.downloading == false && IsDownloadNeed()) {
            	Factory.getFactory().execForRunnableTask(new AlexaDownloader());
            	
            	ChainRunnerPopularity.downloading = true;
            }
            
            boolean bFound = false;
            int nValue = 0;

            // lock in case a switch happens right now
            synchronized (ChainRunnerPopularity.class) {  
	            if (mSitePopulatiry.containsKey(domainToAnalyze.getDomainName())) {
	                bFound = true;
	                logger.info("Domain rank found.");
	            } else {
	                bFound = false;
	                logger.info("Domain rank not found.");
	            }
            }

            Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();

            if (bFound) {
                nValue = mSitePopulatiry.get(domainToAnalyze.getDomainName());
                nValue = showPos == true ? nValue : 1;
            }
            Feature feature = featuresMap.get(FEATURE_NAME);
            if (bFound) {
                feature.setValue(nValue);
            } else {
                feature.setValue(0);
            }
            featuresMap.put(feature.getName(), feature);

            logger.info("Finished checking domain. rank is " + nValue);
        } catch (Exception e) {
            logger.error("caught exception ", e);
            setStatus(ProcessingChain.chainStatus.ERROR);
        }
        flush();
    }

    /**
     * This function loads the hashmap from the csv
     */
    private void LoadHashMap() {

        FileReader frLoad = null;
        BufferedReader brFile = null;
        String strSeperator = ",";
        String strLine = "";
        int nCounter = 0;

        try {

            frLoad = new FileReader(FILE_DIR + "/" + FILE_NAME_OUTPUT);
            brFile = new BufferedReader(frLoad);
            logger.info("Loading Popularity HashMap");
            
            mSitePopulatiry.clear();

            // scan all the csv file and insert the data into a hashamp
            for (nCounter = 0; nCounter < HASH_SIZE && ((strLine = brFile.readLine()) != null); ++nCounter) {
                String[] strRank = strLine.split(strSeperator);
                mSitePopulatiry.put(strRank[1], parseInt(strRank[0]));
            }
            logger.info("Finished loading.");
            brFile.close();

        } catch (FileNotFoundException ex) {
            logger.error("caught exception ", ex);
        } catch (IOException ex) {
            logger.error("caught exception ", ex);
        }
    }

    /**
     * This function loads the HashMap by a lock. only on thread can load the
     * HashMap
     */
    private void switchHashMap() {
        // take lock so no one can read the map while doing
    	// the switch
        synchronized (ChainRunnerPopularity.class) {
        	logger.info("switching the file under lock");
            LoadHashMap();
        }
    }

    /**
     * This function checks if 24 hours have passed since the last file update
     *
     * @return true - 24 hours passed, false - if not
     */
    private boolean IsDownloadNeed() {
    	Date dNow = new Date();
        long today = dNow.getTime();
        long lDiff = (today - lastMod.getTime()) / (60 * 1000);

        // The difference is more the 24 hours
        if (lDiff >= (60*24)) {
            logger.info("A new version of top site avaliable.");
            return true;
        } else {
            return false;
        }

    }

	private void updateLastModifiedTime(File flCheck) {
		Date lastMod = new Date(flCheck.lastModified());
		ChainRunnerPopularity.lastMod = lastMod;
		
		logger.info("updating last modified time to " + lastMod);
	}
}
