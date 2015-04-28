package il.ac.mta.bi.dmd.lookup;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsLookup {
	
	static Logger logger = Logger.getLogger(DnsLookup.class);
	
	/**Creates a DNS query by type. This function should be used if it is necessary to query the DNS server
	 * for messages of type other then A, e.g: type PTR for reverse DNS lookups. For type A queries only, the safer DNSLookupTypeA
	 * should be used
	 * @param host the hostname
	 * @param dnsServerIpAddress the IP address of the DNS server
	 * @param type the DNS message query type
	 * @return a Record array containing all the answers from the server or null if an error occurred
	 * @throws IOException 
	 */
	public Record [] lookupByType(String host, String dnsServerIpAddress, int type, int requestTimeout) throws IOException {
		Record answers [] = null;
		Record rec = null;
		
        	if(type == Type.PTR) {
				rec = Record.newRecord(ReverseMap.fromAddress(host), Type.PTR, DClass.IN);
        	} else {
				rec = Record.newRecord(new Name(host), type, DClass.IN);
    		}
        	
	        Message query = Message.newQuery(rec);
	        SimpleResolver simpleResolver = new SimpleResolver(dnsServerIpAddress);
	        simpleResolver.setTimeout(requestTimeout);
	        
	        Message response = simpleResolver.send(query);
	        answers = response.getSectionArray(Section.ANSWER);

		if (type == Type.PTR && answers == null) {
			logger.info("No DNS reverse-lookup answers from " + dnsServerIpAddress + " for " + host);
		} else if (answers == null){
			logger.info("No DNS lookup answers from " + dnsServerIpAddress + " for " + host);
		}
		
		return answers;
	}
	
	/**Creates a DNS query of type A.
	 * @param host the hostname
	 * @param dnsServerIpAddress the IP address of the DNS server
	 * @param type the DNS message query type
	 * @return a Record array containing only type A answers from the server or null if an error occurred
	 * @throws UnknownHostException 
	 * @throws TextParseException 
	 */
	public Record [] lookupTypeA(String domainName, String dnsServerIpAddress, int requestTimeout) throws UnknownHostException, TextParseException {
		Record answers [] = null;
		
		SimpleResolver simpleResolver = new SimpleResolver(dnsServerIpAddress);
		Lookup dnsLookupService = new Lookup(domainName);
		dnsLookupService.setCache(new Cache());
		simpleResolver.setTimeout(requestTimeout);
		
		dnsLookupService.setResolver(simpleResolver);
		answers = dnsLookupService.run();

		if (answers == null) {
			logger.info("No DNS lookup answers returned from " + dnsServerIpAddress + " for " + domainName);
		}
		
		return answers;
	}

}
