package il.ac.mta.bi.dmd.data.sources;

import java.io.BufferedReader;
import java.io.ObjectOutputStream;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.IClientHandler;
import il.ac.mta.bi.dmd.common.SimpleServer;

public class UiDataSource extends DataSource implements IClientHandler  {
	private Integer listeningPort;
	private SimpleServer serverWrapper;
	
	public UiDataSource(Integer listeningPort) {
		this.listeningPort = listeningPort;
		this.serverWrapper = new SimpleServer(listeningPort, this);
		
		serverWrapper.setServerDescription("Ui source service");
		serverWrapper.start();
	}

	@Override
	public void handle(BufferedReader in, ObjectOutputStream out) throws Exception {
		System.out.println("User load domain names, usage: <domain_name> [classification]. To exit, type \"$ exit\"");

		while (true) {
		    String line = in.readLine();
		    
		    if (line.equals("$ exit")) {
		    	System.out.println("bye!");
		    	return;
		    }
		    if (line.isEmpty()) {
		    	continue;
		    }
		    
		    System.out.println("loaded '" + line + "'");
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
