package il.ac.mta.bi.dmd.dictionary.ahocorasick.trie;

import il.ac.mta.bi.dmd.dictionary.ahocorasick.interval.Interval;
import il.ac.mta.bi.dmd.dictionary.ahocorasick.interval.Intervalable;

public class Emit extends Interval implements Intervalable {

    private final String keyword;

    public Emit(final int start, final int end, final String keyword) {
        super(start, end);
        this.keyword = keyword;
    }

    public String getKeyword() {
        return this.keyword;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword;
    }

}
