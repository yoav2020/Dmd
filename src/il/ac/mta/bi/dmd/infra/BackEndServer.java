package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;

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
		DomainToAnalyze a1 = Factory.getFactory().getDmainToAnalyze("cnn.com", Classification.BENIGN);
		dispatchQueue.add(a1);
		DomainToAnalyze b1 = Factory.getFactory().getDmainToAnalyze("ynet.co.il", Classification.BENIGN);
		dispatchQueue.add(b1);
		DomainToAnalyze c1 = Factory.getFactory().getDmainToAnalyze("mobile.bitterstrawberry.org", Classification.MALICIOUS);
		dispatchQueue.add(c1);
		DomainToAnalyze d1 = Factory.getFactory().getDmainToAnalyze("microsoft.com", Classification.BENIGN);
		dispatchQueue.add(d1);
		DomainToAnalyze e1 = Factory.getFactory().getDmainToAnalyze("www.cellphoneupdated.com", Classification.MALICIOUS);
		dispatchQueue.add(e1);
		
		/* PLACE HOLDER END */
		dispatcher.run();

		logger.info("backend end");
		fini();
	}
	
	protected void fini() {
		Factory.getFactory().shutDownFactory();
	}
}
