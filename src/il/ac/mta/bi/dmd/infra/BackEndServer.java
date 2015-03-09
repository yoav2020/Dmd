package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.common.DomainToAnalyze.Classification;
import il.ac.mta.bi.dmd.data.sources.SimpleFileDataSource;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class BackEndServer {
	
	static Logger logger = Logger.getLogger(BackEndServer.class);

	private LinkedBlockingQueue<DomainToAnalyze> dispatchQueue = 
			new LinkedBlockingQueue<DomainToAnalyze>();
	private Dispatacher dispatcher = new Dispatacher(dispatchQueue);

	public void start() {
		logger.info("backend start");
		
		/* edit \data\input.txt to load domains from files by fetcher */
		initFetcher();
		
		/* PLACE HOLDER START */
		DomainToAnalyze a1 = Factory.getFactory().getDomainToAnalyze("app.pho8.com", Classification.UNKNOWN);
		dispatchQueue.add(a1);
		
		/* PLACE HOLDER END */
		dispatcher.run();

		logger.info("backend end");
		fini();
	}

	private void initFetcher() {
		Fetcher fetcher = Fetcher.getFetcher();
		fetcher.setDispatchQueue(dispatchQueue);
		fetcher.init();
		
		/* SimpleFileDataSource */
		SimpleFileDataSource dataSource = new SimpleFileDataSource();
		dataSource.setFileName("data\\input.txt");
		fetcher.addSource(dataSource);
	}
	
	protected void fini() {
		Factory.getFactory().shutDownFactory();
	}
}
