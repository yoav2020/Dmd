package il.ac.mta.bi.dmd.common;

import java.io.BufferedReader;
import java.io.PrintWriter;

public interface IClientHandler {
	void handle(BufferedReader in, PrintWriter out) throws Exception;
}
