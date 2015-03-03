package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class Fetcher implements Runnable {
	static Logger logger = Logger.getLogger(Fetcher.class);

	
	private static Fetcher theFetcher = null;
	private LinkedBlockingQueue<DomainToAnalyze> dispatchQueue;
	private List<DataSource> sources = new CopyOnWriteArrayList<>();
	
	public static Fetcher getFetcher() {
		if (theFetcher == null) {
			theFetcher = new Fetcher();
		}
		return theFetcher;
	}
	
	public void addSource(DataSource source) {
		logger.info("added data source " + source.getSourceName());
		source.setDispatchQueue(dispatchQueue);
		sources.add(source);
	}
	
	public void removeSource(DataSource source) {
		sources.remove(source);
	}
	
	public void init() {
		logger.info("fetcher started");
		Factory.getFactory().execFixedPerodicRunnableTask(this, 0, 2, TimeUnit.SECONDS);
	}
	
	private Fetcher(){
	}

	public LinkedBlockingQueue<DomainToAnalyze> getDispatchQueue() {
		return dispatchQueue;
	}

	public void setDispatchQueue(LinkedBlockingQueue<DomainToAnalyze> dispatchQueue) {
		this.dispatchQueue = dispatchQueue;
	}

	@Override
	public void run() {
		for (DataSource source : sources) {
			source.run();
		}
	}
}
