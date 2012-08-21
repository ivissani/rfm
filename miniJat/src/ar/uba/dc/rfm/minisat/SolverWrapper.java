package ar.uba.dc.rfm.minisat;

public class SolverWrapper extends Solver {
	static {
		// Static code for loading library
		// workaround for odd behavior in scala interpreter
		// @see http://www.scala-lang.org/node/1043
		System.loadLibrary("minisat");
	}
}
