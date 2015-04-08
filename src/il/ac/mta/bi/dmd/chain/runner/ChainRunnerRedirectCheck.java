package il.ac.mta.bi.dmd.chain.runner;

import com.google.common.net.InternetDomainName;
import static il.ac.mta.bi.dmd.chain.runner.ChainRunnerValidate.logger;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.common.ProcessingChain;
import il.ac.mta.bi.dmd.infra.Factory;
import java.io.IOException;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 * This chain checks for redirects
 * @author Mike
 */
public class ChainRunnerRedirectCheck extends ProcessChain {

    static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
    private String FEATURE_NAME = "IsRedirect";

    public ChainRunnerRedirectCheck() {
        setChainName(FEATURE_NAME + " checker");
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(FEATURE_NAME));
    }

    @Override
    public void run() {
        logger.info("Getting " + domainToAnalyze.getDomainName() + " for redirect check.");

        // Get the redirect domain
        HttpURLConnection httpConnection;
        boolean bError = false;
        boolean bRedirect = false;
        String strUrlHTTP = "";
        String strUrlHTTPS = "";
        String strUrl = "";
        String strDomain = domainToAnalyze.getDomainName();

        // Check if the domain contains http or https, if not, add.
        if (!(strDomain.contains("http://") || strDomain.contains("https://"))) {
            strUrlHTTP = "http://" + domainToAnalyze.getDomainName(); // adding protocol
            strUrlHTTPS = "https://" + domainToAnalyze.getDomainName(); // adding protocol
        }
        
        String strLocation = strDomain;
        Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();
        Feature feature = featuresMap.get(FEATURE_NAME);
        
        
        /**
         * Check for HTTP REQUESTS //TODO: add support for HTTPS
         */
        
        strUrl = strUrlHTTP;
        // Try to check the http request
        try {

            httpConnection = (HttpURLConnection) (new URL(strUrl).openConnection()); //
            httpConnection.setInstanceFollowRedirects(false); // dont jump to redirects
            httpConnection.connect();

            int responseCode = httpConnection.getResponseCode();
            if (responseCode == 301 || responseCode == 302 || responseCode == 303) {
                bRedirect = true;
                strLocation = httpConnection.getHeaderField("Location");
                domainToAnalyze.setDomainName(strLocation);
                // Update the features map
                feature.setValue(1);
                domainToAnalyze.getPropertiesMap().put("OldDomain", strDomain);
                domainToAnalyze.getPropertiesMap().put("NewDomain", strLocation);
            }
            else
               // Update the features map
                feature.setValue(0); 

        } catch (MalformedURLException ex) {
            logger.info(strUrl + " is unformated url. ");
            bError = true;
            java.util.logging.Logger.getLogger(ChainRunnerRedirectCheck.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            // Host not found
            logger.info(strUrl + " is unknown host.");
            bError = true;
            java.util.logging.Logger.getLogger(ChainRunnerRedirectCheck.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ChainRunnerRedirectCheck.class.getName()).log(Level.SEVERE, null, ex);
            setStatus(ProcessingChain.chainStatus.ERROR);
        }

        // Exit
        logger.info("Finished checking domain. Redirected domain ? " + bRedirect);
        flush();
    }

}
