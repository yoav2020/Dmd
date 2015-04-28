package il.ac.mta.bi.dmd.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

public class WhoisLookup {
	  static Logger logger = Logger.getLogger(WhoisLookup.class);
	  final int port = 43;
	  
		/**Constructs an extended, bulk whois request, used to perform large whois queries
		 * for some whois service provider, such as Cymru.
		 * @param queries the IP addresses to be sent to the whois server
		 * @return the bulk-request, in the correct format
		 */
	  String createBulkRequest(Set<String> queries) {
		  StringBuilder bulkRequest = new StringBuilder();
		  bulkRequest.append("begin");
		  bulkRequest.append("\n");
		  bulkRequest.append("verbose");
		  bulkRequest.append("\n");
		  
		  for (String query : queries) {
			  bulkRequest.append(query);
			  bulkRequest.append("\n");
		  }
		  
		  bulkRequest.append("end");
		  
		  return bulkRequest.toString();
	  }
	  
		/**Constructs a single whois request,
		 * @param whoIsServer the Whois server to be queried
		 * @param query the IP address we want to perform the Whois query on
		 * @return the response to the query
		 * @throws IOException 
		 */
	  ArrayList<String> lookup(String whoIsServer, String query) throws IOException {
		  Socket connection = null;
		  ArrayList<String> whoIsResponse= new ArrayList<String>();
		  
		  if (query == null ) {
			  logger.warn("No whois query provided!");
			  return whoIsResponse;
		  }
		  
		  try {
			  connection = new Socket(whoIsServer, port);
			  String line = null;
			  connection.setSoTimeout(10000);
			  
		      PrintStream out = new PrintStream(connection.getOutputStream());
		      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		      out.println(query);
			  connection.shutdownOutput();
		      
			  while ((line = in.readLine()) != null) {
			      whoIsResponse.add(line);	 
			  }
			  connection.shutdownInput();
		  } finally {
			  if (connection != null) {
				  try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			  }
		  }
		  return whoIsResponse;
	  }
	 
		/**Performs a bulk-request, suitable for Whois queries to Cymru Whois servers.
		 * @param whoIsServer the Whois server to be queried
		 * @param queries a Set of queries to be sent
		 * @return the response to the query
		 * @throws IOException 
		 * @throws UnknownHostException 
		 */
	  public ArrayList<String> bulkLookup(String whoIsServer, Set<String> queries) throws UnknownHostException, IOException {
		  Socket connection = null;
		  ArrayList<String> whoIsResponse= new ArrayList<String>();
		  
		  try {
			  if (queries == null || queries.size() == 0) {
				  logger.warn("No whois queries provided!");
				  return whoIsResponse;
			  }
			  connection = new Socket(whoIsServer, port);
			  String line = null;
			  connection.setSoTimeout(10000);
			  
		      PrintStream out = new PrintStream(connection.getOutputStream());
		      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		      out.println(createBulkRequest(queries));
			  connection.shutdownOutput();
		      
			  while ((line = in.readLine()) != null) {
			      whoIsResponse.add(line);	 
			  }
			  connection.shutdownInput();
		  } finally {
			  if (connection != null) {
				  try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			  }
		  }
		  return whoIsResponse;
	 }
	  
	  public ArrayList<WhoisQueryResults> parseWhoisResponse(ArrayList<String> response) {
		  ArrayList<WhoisQueryResults> WhoisQueryResultsList = new ArrayList<>();
		  /* first line in response is status message
		   * so we remove it as it doesn't contain any relevant response
		   */
		  response.remove(0);
		  
		  logger.info("starting to parse results");
		  for (String line : response) {
			  try {
				WhoisQueryResultsList.add(parseWhoisLine(line));
			} catch (ParseException e) {
				logger.warn("failed to process line: " + line, e);
			}
		  }
		  logger.info("returning " + WhoisQueryResultsList.size() + " results");
		  
		  return WhoisQueryResultsList;
	  }

	/*
	 * 	[Querying v4.whois.cymru.com]
		[v4.whois.cymru.com]
		AS      | IP               | BGP Prefix          | CC | Registry | Allocated  | AS Name
		12619   | 192.115.80.55    | 192.115.80.0/22     | IL | ripencc  |            | YEDIOT-AS Yediot Information Technologies ltd,IL
	 */
	  private WhoisQueryResults parseWhoisLine(String line) throws ParseException {
			String [] splitLine = line.split("\\|");
			logger.info(line);
			WhoisQueryResults whoIsQueryResults = new WhoisQueryResults();
		  	  
		  	if (splitLine.length < 7) {
		  		throw new ParseException("invalid response format line", 0);
		  	}
		  	  
		  	whoIsQueryResults.setAsNum(splitLine[0].trim());
		  	whoIsQueryResults.setIpAddr(splitLine[1].trim());
		  	whoIsQueryResults.setBgpPrefix(splitLine[2].trim());
		  	whoIsQueryResults.setCc(splitLine[3].trim());
		  	whoIsQueryResults.setAsOnwer(splitLine[6].trim());
		  	
			return whoIsQueryResults;
		}
}