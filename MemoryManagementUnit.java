import java.io.FileNotFoundException;

public class MemoryManagementUnit {
	/*
	 * First-In-First-Out
	 */
	
	TranslationLookasideBuffer.lookUpResult lookUpResult;
	PageTable.ptResult ptResult;
	int i = 0;
	TranslationLookasideBuffer tlb;
	//PageTable pt;
	OperatingSystem os;
	
	public MemoryManagementUnit(int tlbSize) {
		tlb = new TranslationLookasideBuffer(tlbSize);
	}
	
	public TranslationLookasideBuffer.lookUpResult lookUp(String address) {
		lookUpResult = tlb.lookUp(address);
		
		
		if(lookUpResult.found) {
			//found in the TLB
			return lookUpResult;
		} else {
			//not found in the TLB
			return lookUpResult;
		}
	}
	
	public void addPTEToTLB(String vpn, PageTable.PageTableEntry pte) {
		//this is where we run the FIFO algorithm and evict a certain page
		//System.out.println("adding " + vpn + " to the TLB");
		
		int index = 0;
		
		/**
		 * this is the FIFO selection
		 */
		while(true) {
			if (tlb.TLB[index].ref == false) {
				break;
			} else {
				index++;
				if (index == 8) {
					index = 0;
					resetAllRef();
				}
			}
		}
		/**
		 * END
		 */

		
		tlb.addPTE(index, vpn, pte);
	}

	public void setDirty(String vpn, boolean dir) {
		for(int i = 0; i < tlb.TLB.length; i++) {
			if(tlb.TLB[i].vpnum.equals(vpn)) {
				tlb.TLB[i].dir = true;
				return;
			}
		}
	}

	public void resetAllRef() {
		for(int i = 0; i < tlb.TLB.length; i++) {
			if(tlb.TLB[i].ref == true) {
				tlb.TLB[i].ref = false;
			}
		}
	}
	
	public void printAllTLB( ) {
		for(int i = 0; i < tlb.TLB.length; i++) {
			System.out.println(tlb.TLB[i].vpnum + " >>>> " + tlb.TLB[i].pfnum);
		}
	}
}
