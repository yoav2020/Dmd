package il.ac.mta.bi.dmd.data.sources;

import java.io.BufferedReader;
import java.io.PrintWriter;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.IClientHandler;
import il.ac.mta.bi.dmd.common.ServerWrapper;

public class UiDataSource extends DataSource implements IClientHandler  {
	private Integer listeningPort;
	private ServerWrapper serverWrapper;
	
	public UiDataSource(Integer listeningPort) {
		this.listeningPort = listeningPort;
		this.serverWrapper = new ServerWrapper(listeningPort, this);
		
		serverWrapper.setServerDescription("Ui source service");
		serverWrapper.start();
	}

	@Override
	public void handle(BufferedReader in, PrintWriter out) throws Exception {
		out.println("User load domain names, usage: <domain_name> [classification]. To exit, type \"$ exit\"");

		while (true) {
		    String line = in.readLine();
		    
		    if (line.equals("$ exit")) {
		    	out.println("bye!");
		    	return;
		    }
		    if (line.isEmpty()) {
		    	continue;
		    }
		    
		    out.println("loaded '" + line + "'");
		    addDomainFromSource(line);
		}
	}

	@Override
	public void run() {
	}

	public Integer getListeningPort() {
		return listeningPort;
	}
}
