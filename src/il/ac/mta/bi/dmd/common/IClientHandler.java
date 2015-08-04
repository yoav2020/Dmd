package il.ac.mta.bi.dmd.common;

import java.io.BufferedReader;
import java.io.ObjectOutputStream;

public interface IClientHandler {
	void handle(BufferedReader in, ObjectOutputStream out) throws Exception;
}
