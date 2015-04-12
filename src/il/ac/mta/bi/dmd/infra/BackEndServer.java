package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;
import il.ac.mta.bi.dmd.data.sources.UiDataSource;
import il.ac.mta.dmd.data.targets.UiDataTarget;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class BackEndServer {
	
	static Logger logger = Logger.getLogger(BackEndServer.class);

	private LinkedBlockingQueue<DomainToAnalyze> dispatchQueue = 
			new LinkedBlockingQueue<DomainToAnalyze>();
	private Dispatacher dispatcher = new Dispatacher(dispatchQueue);
	private static Integer userServicePortNum = 4321;

	public void start() {
		logger.info("backend start");
		System.out.println("Server backend started");
		
		/* data source */
		initFetcher();
		
		/* data target */
		initSaver();
		
		dispatcher.run();

		logger.info("backend end");
		System.out.println("Server backend terminated");

		fini();
	}
	
	private void initSaver() {
		Saver saver = Saver.getSaver();
		DataTarget dataTarget = new UiDataTarget(4322);
		dataTarget.setTargetName("UI service target");
		saver.setDataTarget(dataTarget);
	}

	private void initFetcher() {
		Fetcher fetcher = Fetcher.getFetcher();
		fetcher.setDispatchQueue(dispatchQueue);
		fetcher.init();
		
		/* SimpleFileDataSource 
		SimpleFileDataSource dataSource = new SimpleFileDataSource();
		dataSource.setFileName("data\\input.txt");
		//dataSource.setFileName("data\\test_input.txt");
		fetcher.addSource(dataSource);*/
		
		/* uiDataSource */
		UiDataSource uiDataSource = new UiDataSource(userServicePortNum);
		uiDataSource.setSourceName("UI service source");
		fetcher.addSource(uiDataSource);
	}
	
	protected void fini() {
		Factory.getFactory().shutDownFactory();
	}
}
