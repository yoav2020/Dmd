package il.ac.mta.bi.dmd.chain.runner;

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
import static java.lang.Integer.parseInt;
import org.apache.log4j.Logger;
import static java.lang.System.out;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class checks the rank of a given domain. It downloads a csv from alexa
 * website , then unzips it and reads the ranks of the top 1000000 sites. later
 * there is a hash table that is built to support the the fetching of the ranks
 */
public class ChainRunnerPopularity extends ProcessChain {

    static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
    private String FILE_DIR = "CSVExtract";
    private String FILE_NAME = "top-1m-csvsites.zip";
    private String FILE_NAME_OUTPUT = "top-1m.csv";
    private String FILE_URL = "http://s3.amazonaws.com/alexa-static/top-1m.csv.zip";
    private static Map<String, Integer> mSitePopulatiry = new HashMap<String, Integer>();
    private static boolean bLocked = false;
    private int HASH_SIZE = 10000;
    private String FEATURE_NAME = "domainRank";
    private boolean bFirstRun = true;
    public boolean showPos = true;

    public ChainRunnerPopularity() {
        setChainName("Popularity checker");
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(FEATURE_NAME));
    }

    @Override
    public void run() {
        logger.info("Getting " + domainToAnalyze.getDomainName() + " rank.");
        
        try {
	        //Initialize the first run
	        if (bFirstRun) {
	            // Is there a new version needed?
	            if (IsDownloadNeed()) {
	                // Download the new zip
	                DownloadCSV();
	                //Unzip the zip
	                UnzipCSV();
	                // Looad the new hashmap
	                switchHashMap();
	            }
	
	            // Load the new hashmap 
	            if (mSitePopulatiry.size() < 1) {
	                switchHashMap();
	            }
	        }
	        boolean bFound = false;
	        int nValue = 0;
	
	        // Is the domain in the top 1'm?
	        if (mSitePopulatiry.containsKey(domainToAnalyze.getDomainName())) {
	            bFound = true;
	            logger.info("Domain rank found.");
	        } else {
	            bFound = false;
	            logger.info("Domain rank not found.");
	        }
	
	        Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
	        Map<String, Object> propertiesMap = domainToAnalyze.getPropertiesMap();

            if (bFound) {
                nValue = mSitePopulatiry.get(domainToAnalyze.getDomainName());
                nValue = showPos== true ? nValue : 1;
            }
            Feature feature = featuresMap.get(FEATURE_NAME);
            if (bFound) {
                feature.setValue(nValue);
            } else {
                feature.setValue(null);
            }
            featuresMap.put(feature.getName(), feature);
            if (bFound) {
                propertiesMap.put(feature.getName(), nValue);
            } else {
                propertiesMap.put(feature.getName(), null);
            }
            
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

            // scan all the csv file and insert the data into a hashamp
            for (nCounter = 0; nCounter < HASH_SIZE && ((strLine = brFile.readLine()) != null); ++nCounter) {
                String[] strRank = strLine.split(strSeperator);
                mSitePopulatiry.put(strRank[1], parseInt(strRank[0]));
            }
            logger.info("Finished loading.");
            brFile.close();

        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerPopularity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerPopularity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This function loads the hashmap by a lock. only on thread can load the
     * hashmap
     */
    private void switchHashMap() {
        // Someone got a lock so refresh the hashmap
        if (!bLocked) {
            bLocked = true;
            mSitePopulatiry.clear();
            LoadHashMap();
            bLocked = false;
        }
    }

    /**
     * This function checks if 24 hours have passed since the last file update
     *
     * @return true - 24 hours passed, false - if not
     */
    private boolean IsDownloadNeed() {
        File flCheck = new File(FILE_NAME);
        Date lastMod = new Date(flCheck.lastModified());
        Date dNow = new Date();
        long today = dNow.getTime();

        long lDiff = (today - lastMod.getTime()) / (60 * 1000);

        // The diffrence is more the 24 hours
        if (lDiff >= (60 * 24)) {
            logger.info("A new version of top site avaliable.");
            return true;
        } else {
        	logger.info("Top sites version is latest.");
            return false;
        }

    }

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
            out.println("Dowinloading " + FILE_NAME + " Total download size : " + nTotalDownloadSize + " KB");

            // Read 1024 bytes everytime 
            while ((nByteCount = bufferInpStream.read(bData)) != -1) {
                bufferOutStream.write(bData, 0, nByteCount);
                lTotalData += nByteCount;
                nProgress = (((float) lTotalData * 100 / lFileLength));
                nProgress = Math.round(nProgress);
                if (nProgress % 10 == 0 && fLast < nProgress) {
                    out.println("Total progress: %" + nProgress);
                    fLast = nProgress;
                }
            }
            out.println("Download finished.");

            bufferOutStream.flush();
            bufferOutStream.close();

            // Set the last mod on the file as the current date
            Date now = Calendar.getInstance().getTime();
            flsSave.setLastModified(now.getTime());

        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerPopularity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerPopularity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This function unzips the given zip file
     */
    private void UnzipCSV() {

        String zipFile = FILE_NAME;
        String outputFolder = FILE_DIR;

        byte[] buffer = new byte[1024];

        try {

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
            out.println("Unziping " + FILE_NAME);
            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("Unziping : " + newFile.getAbsoluteFile());

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

            System.out.println(FILE_NAME + " Unziped.");
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerPopularity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerPopularity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
