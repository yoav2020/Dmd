package il.ac.mta.bi.dmd.main;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Program {
	static Logger logger = Logger.getLogger(Program.class);

	/**
	 * Main
	 * @param args
	 */
    public static void main(String[] args) {
    	logger.info("program start");
        SpringApplication.run(Program.class, args);
    }
}
/*public class Program {
	
	static Logger logger = Logger.getLogger(Program.class);


	public static void main(String[] args) {
		logger.info("program start");
		BackEndServer server = new BackEndServer();
		server.start();
	}

}*/
