import java.util.HashSet;
import java.util.Set;

import ar.uba.dc.rfm.minisat.ClauseInfo;
import ar.uba.dc.rfm.minisat.Solver;
import ar.uba.dc.rfm.minisat.clauseseq;
import ar.uba.dc.rfm.minisat.intseq;


public class Main {
	public static int[] intseqToArray(intseq is)
	{
		int[] iArr = new int[new Long(is.size()).intValue()];
		
		for(int i = 0; i < is.size(); i++)
			iArr[i] = is.get(i);
		
		return iArr;
	}
	
	public static Set<ClauseInfo> clauseseqToSet(clauseseq c) {
		Set<ClauseInfo> s = new HashSet<ClauseInfo>();
		
		for(int i = 0; i < c.size(); i++)
			s.add(c.get(i));
		
		return s;
	}
	
	public static void main(String[] args) {
		System.loadLibrary("minisat");
		
		Solver s = new Solver();
		
		long start = System.currentTimeMillis();
		
		s.read("/home/ivissani/RFM/miscosas/minisat/cnf/p8.cnf");
		s.set_time_budget(10);
		s.solve_limited(new intseq());
		
		s.set_time_budget(10);
		s.solve_limited(new intseq());
		
		
		long time = System.currentTimeMillis() - start;
		
		System.out.println((double)time/1000);
		
		clauseseq cs = new clauseseq();
		s.get_learnts(cs);
		
		s.set_conf_budget(100);
		s.solve_limited(new intseq());
		s.get_learnts(cs);
		
		for(ClauseInfo ci : clauseseqToSet(cs))
		{
			System.out.println("{ ");
			System.out.println("\t"+ci.getLBD());
			System.out.println("\t"+ci.getActivity());
			System.out.print("\t"+"[ ");
			for(int i : intseqToArray(ci.getLiterals()))
			{
				System.out.print(i + " ");
			}
			System.out.println("] ");
			System.out.println("}");
		}
	}
}
