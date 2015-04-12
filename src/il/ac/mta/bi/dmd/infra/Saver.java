package il.ac.mta.bi.dmd.infra;

import il.ac.mta.bi.dmd.common.DataSource;
import il.ac.mta.bi.dmd.common.DataTarget;
import il.ac.mta.bi.dmd.common.DomainToAnalyze;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

public class Saver {
static Logger logger = Logger.getLogger(Saver.class);

	private static Saver theSaver = null;
	private List<DataTarget> targets = new CopyOnWriteArrayList<>();
	
	public static Saver getSaver() {
		if (theSaver == null) {
			theSaver = new Saver();
		}
		return theSaver;
	}
	
	public void setDataTarget(DataTarget target) {
		logger.info("added data target " + target.getSourceName());
		targets.add(target);
	}
	
	public void removeSaver(DataSource source) {
		targets.remove(source);
	}
	
	private Saver(){
	}


	public void save(DomainToAnalyze domainToAnalyze) {
		for (DataTarget target : targets) {
			target.save(domainToAnalyze);
		}
	}
}
