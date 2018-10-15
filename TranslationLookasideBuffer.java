
public class TranslationLookasideBuffer {
	TLBEntry[] TLB;
	
	public TranslationLookasideBuffer(int num) {
		TLB = new TLBEntry[num];
		
		for(int i = 0; i < TLB.length; i++) {
			TLB[i] = new TLBEntry(null, false, false, false, -1);
		}
		

		//TLB[0] = new TLBEntry("1C", true, true ,true, 0);
	}
	
	public lookUpResult lookUp(String vpnum) {
		for(int i = 0; i < TLB.length; i++) {
			if(vpnum.equals(TLB[i].vpnum)) {
				//System.out.println(vpnum + " found in TLB at index " + i);
				//System.out.println("the page number is " + TLB[i].vpnum + "  " + i);
				return new lookUpResult(true, TLB[i]);
			} else {
				
			}
		}
		//System.out.println(vpnum + " not in TLB");
		
		return new lookUpResult(false, TLB[0]);
	}
	
	public void addPTE(int index, String vpn, PageTable.PageTableEntry pte) {
		TLB[index].vpnum = vpn;
		TLB[index].val = true;
		TLB[index].ref = true;
		TLB[index].dir = false;
		TLB[index].pfnum = pte.pfnum;
	}
	
	
	class lookUpResult {
		boolean found;
		TLBEntry result;
		
		public lookUpResult(boolean found, TLBEntry result) {
			this.found = found;
			this.result = result;
		}
	}

	public class TLBEntry {
		String vpnum; 	//represents the virtual page number
		boolean val;	//represents the valid bit
		boolean ref;	//represents the reference bit
		boolean dir;	//represents the dirty bit
		int pfnum;		//represents the page frame number
		
		public TLBEntry(String a, boolean b, boolean c, boolean d, int e) {
			vpnum = a;
			val = b;
			ref = c;
			dir = d;
			pfnum = e;
		}
		
	}
	
}
