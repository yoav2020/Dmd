package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class BackEndServer {
	
	static Logger logger = Logger.getLogger(BackEndServer.class);

	private LinkedBlockingQueue<DomainToAnalyze> dispatchQueue = 
			new LinkedBlockingQueue<DomainToAnalyze>();
	private Dispatacher dispatcher = new Dispatacher(dispatchQueue);

	public void start() {
		logger.info("backend start");
		
		/* PLACE HOLDER START */
		DomainToAnalyze a1 = Factory.getFactory().getDmainToAnalyze("cnn.com");
		dispatchQueue.add(a1);
		
		/* PLACE HOLDER END */
		dispatcher.run();

		logger.info("backend end");
		fini();
	}
	
	protected void fini() {
		Factory.getFactory().shutDownFactory();
	}
}
