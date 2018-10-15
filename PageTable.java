
public class PageTable {
	PageTableEntry[] pageTable;
	
	public PageTable(int numPages) {
		pageTable = new PageTableEntry[numPages];
		
		for(int i = 0; i < pageTable.length; i++) {
			pageTable[i] = new PageTableEntry(false, false, false, i % 16);
			//System.out.println(i % 16);
		}
	}
	
	
	public ptResult lookUp(String vpnum) {
		//convert string to number
		//use number as index
		int vpIndex;
		
		vpnum = vpnum.toLowerCase();
		vpIndex = Integer.parseInt(vpnum, 16);
		//System.out.println(vpIndex);
		
		//now look up pageTable[vpIndex]
		if(pageTable[vpIndex].val) {
			//the corresponding page frame # is valid and should be returned
			return new ptResult(true, pageTable[vpIndex]);
		} else {
			//the corresponding page frame # is invalid and the page has to be brought in from memory
			return new ptResult(false, null);
		}
	}
	
	class ptResult {
		boolean found;
		PageTableEntry pte;
		
		public ptResult(boolean found, PageTableEntry result) {
			this.found = found;
			this.pte = result;
		}
	}

	public class PageTableEntry {
		boolean val;	//represents the valid bit
		boolean ref;	//represents the reference bit
		boolean dir;	//represents the dirty bit
		int pfnum;		//represents the page frame number in physical memory
		
		public PageTableEntry(boolean val, boolean ref, boolean dir, int pfnum) {
			this.val = val;
			this.ref = ref;
			this.dir = dir;
			this.pfnum = pfnum;
		}
	}
}


