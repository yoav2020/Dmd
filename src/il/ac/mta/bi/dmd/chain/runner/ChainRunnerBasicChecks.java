package il.ac.mta.bi.dmd.chain.runner;

import com.google.common.net.InternetDomainName;
import il.ac.mta.bi.dmd.common.Feature;
import il.ac.mta.bi.dmd.common.ProcessChain;
import il.ac.mta.bi.dmd.infra.Factory;
import static java.lang.System.out;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This Chain does some basic checks
 *
 * @author Mike
 */
public class ChainRunnerBasicChecks extends ProcessChain {

    static Logger logger = Logger.getLogger(ChainRunnerDnsLookup.class);
    private String DOMAIN_LENGTH = "DomainLength";
    private String NUMBER_DOTS = "NumberOfDots";
    private String DOMAIN_IS_IP = "DomainIsIP";

    private static final String IPADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public ChainRunnerBasicChecks() {
        setChainName("TopLevelDomain checker");
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(DOMAIN_LENGTH));
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(NUMBER_DOTS));
        chainFeaturesList.add(Factory.getFactory().getIntegerFeature(DOMAIN_IS_IP));
    }

    @Override
    public void run() {
        logger.info("Getting " + domainToAnalyze.getDomainName() + " basic checks.");
        String strDomain = domainToAnalyze.getDomainName();

        // Getting length
        int nLength = strDomain.length();

        // Check for ip address
        int nIsIpAdd = 0;
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(strDomain);

        if (matcher.matches() == true) {
            // The domain is IP address
            nIsIpAdd = 1;
        }

        int nDotsCount = StringUtils.countMatches(strDomain, ".");

        Map<String, Feature> featuresMap = domainToAnalyze.getFeaturesMap();

        // Update the map
        featuresMap.get(DOMAIN_LENGTH).setValue(nLength);
        featuresMap.get(NUMBER_DOTS).setValue(nDotsCount);
        featuresMap.get(DOMAIN_IS_IP).setValue(nIsIpAdd);
        logger.info("Domain length : " + nLength);
        logger.info("Domain is IP? : " + nIsIpAdd);
        logger.info("Number of dots in domain : " + nDotsCount);

        logger.info("Finished checking domain. ");
        flush();
    }

}
