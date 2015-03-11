package il.ac.mta.bi.dmd.dictionary.ahocorasick.interval;

public interface Intervalable extends Comparable {

    public int getStart();
    public int getEnd();
    public int size();

}
