import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CentralProcessingUnit {
	MemoryAccessCall[] calls;
	MemoryManagementUnit mmu;
	OperatingSystem os;
	PageTable.ptResult ptResult;
	TranslationLookasideBuffer.lookUpResult tlbResult;
	CSV csv;
	
	String outputFileName;
	
	public CentralProcessingUnit(int tlbSize, int ptSize, int memoryAddressWidthBits, int pageOffsetBits) {
		mmu = new MemoryManagementUnit(tlbSize);
		os = new OperatingSystem(ptSize, memoryAddressWidthBits, pageOffsetBits);
	}
	
	public void run() throws IOException {
		
		for(int i = 0; i < calls.length; i++) {
			//System.out.println("\nRUN " + i);
			
			runInstruction(calls[i]);
			
			//mmu.printAllTLB();
			//os.printValPages();
			
			if(i % 5 == 4) {
				//System.out.println("REF GETS RESET");
				os.resetAllRef();	//page table reference vals
			}
		}
		
		csv.close();
	}
	
	
	
	
	public void runInstruction(MemoryAccessCall m) throws IOException {
		//System.out.println("\nINSTRUCTION:  (" + m.call + ") - " + m.address);
		
		TranslationLookasideBuffer.lookUpResult	lookUpResult;
		MemoryAccessCall curCall = m;
		int vpn;
		String vpn_16;
		int pfnum;
		int offset;
		PageTable.PageTableEntry curPTE;
		String evict;
		
		vpn_16 = m.address.substring(0, 2);
		vpn = Integer.parseInt(vpn_16, 16);
		offset = Integer.parseInt(curCall.address.substring(2, 4), 16);
		
		csv.address = m.address;
		csv.rw = Integer.toString(m.call);
		
		//TLB look up
		lookUpResult = mmu.lookUp(vpn_16);
		
		if(lookUpResult.found) {		//THIS IS A HIT, PAGE IS IN TLB
			csv.hit = "1";
			
			executeCommand(curCall);
			pfnum = mmu.tlb.lookUp(curCall.address.substring(0, 2)).result.pfnum;
			os.setValid(pfnum, true);
			
			
		} else {	//check the page table
			
			//PT look up
			ptResult = os.pt.lookUp(vpn_16);	
			
			if(ptResult.found) {	//THIS IS A SOFT MISS, PAGE IS IN PAGE TABLE
				csv.soft = "1";
				
				mmu.addPTEToTLB(vpn_16, ptResult.pte);
				
				vpn = Integer.parseInt(mmu.tlb.lookUp(curCall.address.substring(0, 2)).result.vpnum, 16);
				
				executeCommand(curCall);
				os.setValid(vpn, true);
			} else {				//THIS IS A HARD MISS, PAGE HAS TO BE LOADED FROM DISK
				csv.hard = "1";
				
				pfnum = os.load(vpn_16);

				if(os.checkEvicted()) {
					if (os.evictedVP < 16) {
						csv.evicted_pg = "0" + Integer.toHexString(os.evictedVP).toUpperCase();
					} else {
						csv.evicted_pg = Integer.toHexString(os.evictedVP).toUpperCase();
					}
				}
				
				csv.dirty = Integer.toString(os.evictedDirty);
				
				curPTE = os.addToPT(vpn, pfnum);
				mmu.addPTEToTLB(vpn_16, curPTE);
				
				executeCommand(curCall);
				os.setValid(vpn, true);
			}
		}
		
		csv.write();
	}
	
	
	public void executeCommand(MemoryAccessCall curCall) {
		int pfnum;
		int offset;
		int value;
		String vpn_16;
		int vpn;
		
		pfnum = mmu.tlb.lookUp(curCall.address.substring(0, 2)).result.pfnum;
		offset = Integer.parseInt(curCall.address.substring(2, 4), 16);
		value = curCall.value;
		vpn_16 = curCall.address.substring(0, 2);
		vpn = Integer.parseInt(vpn_16, 16);
		
		switch(curCall.call) {
			case 0:
				//read in the data and save it to the csv
				csv.value_rw = Integer.toString(os.read(pfnum, offset));
				break;
			case 1:
				//write data
				os.write(pfnum, offset, value);
				csv.value_rw = Integer.toString(value);
				
				//also update TLB and PT dirty bits
				os.setDirty(vpn, true);
				mmu.setDirty(vpn_16, true);
				break;
		}
	}
	
	
	public void setOutputFileName (String name) throws IOException{
		outputFileName = name + "_output.csv";
		csv = new CSV(outputFileName);
	}
	
	
	public void readTestFile(File f) throws FileNotFoundException {
		Scanner reader = new Scanner(f);
		
		//count how many calls exist in the file
		int n = 0;
		int curCall;
		while(reader.hasNext()) {
			curCall = reader.nextInt(); n++;
			
			if(curCall == 0) {
				reader.next(); //skip the address, count it
			} else { //cur == 1
				reader.next(); //skip the address
				reader.next(); //skip the value
			}			
		}
		
		//organize the data into an array of MemoryAccessCall objects
		reader = new Scanner(f);
		int i = 0;
		String curAddress;
		int curValue;
		MemoryAccessCall[] calls = new MemoryAccessCall[n];
		while(reader.hasNext()) {
			curCall = reader.nextInt();
			if(curCall == 0) {	//READ
				curAddress = reader.next(); 	//record the address
				calls[i] = new MemoryAccessCall(curCall, curAddress, 0);		//initializing the new call object
				i++;
			} else { 			//WRITE
				curAddress = reader.next(); 	//record the address
				curValue = reader.nextInt(); 	//record the value
				calls[i] = new MemoryAccessCall(curCall, curAddress, curValue);	//initializing the new call object
				i++;
			}			
		}
		
		this.calls = calls;
	}
	
	public class CSV {
		String address;
		String rw;
		String value_rw;
		String soft = "0";
		String hard = "0";
		String hit = "0";
		String evicted_pg;
		String dirty;
		FileWriter file;
		PrintWriter pw;
		
		public CSV (String filename) throws IOException {
			file = new FileWriter(filename);
			pw = new PrintWriter(file);
			
			pw.println("Address,r/w,value,soft,hard,hit,evicted_pg#,dirty_evicted_page");
		}
		
		public void write() {
			pw.println("=\"" +  address + "\"," + rw + "," + value_rw + "," + soft + "," 
					+ hard + "," + hit + ",=\"" + evicted_pg + "\"," + dirty);
			
			//reset all values
			address = rw = value_rw = evicted_pg = dirty = null;
			soft = hard = hit = "0";
		}
		
		public void close() throws IOException {
			file.close();
			pw.close();
		}
	}
}
