package il.ac.mta.bi.dmd.main;

import il.ac.mta.bi.dmd.infra.BackEndServer;

import org.apache.log4j.Logger;

public class Program {
	
	static Logger logger = Logger.getLogger(Program.class);

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("program start");
		BackEndServer server = new BackEndServer();
		server.start();
	}

}
