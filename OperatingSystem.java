import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class OperatingSystem {
	/*
	 * Clock algorithm for page replacement
	 * You must implement the clock replacement algorithm's data structure as a circular linked list
	 * 
	 * Resets the r-bit every 5 instructions
	 */
	Memory ram;
	int ptSize;
	PageTable pt;
	int instructionCounter;
	
	boolean evict;
	int evictedVP;
	int evictedDirty;
	
	public OperatingSystem(int ptSize, int memoryAddressWidthBits, int pageOffsetBits) {
		this.ptSize = ptSize;
		pt = new PageTable(ptSize);
		setMemory(memoryAddressWidthBits, pageOffsetBits);
	}
	
	
	public boolean checkEvicted( ) {
		if(evict) {
			evict = false;
			return true;
		}
		
		return false;
	}
	
	public void setMemory(int memoryAddressWidthBits, int pageOffsetBits) {
		ram = new Memory((int) Math.pow(2, memoryAddressWidthBits - pageOffsetBits), (int) Math.pow(2, pageOffsetBits));
	}
	
	
	public void evictPage(String page) {
		System.out.println("evicting " + page + " from the page table");
		int vpn = Integer.parseInt(page, 16);
		pt.pageTable[vpn].val = false;
	}
	
	
	public int load(String page) throws IOException {
		//System.out.println("loading page " + page + " from disk");
		File file = new File("new_page_files/" + page + ".pg");
		Scanner reader = new Scanner(file);
		
		//select which page frame #
		int pfnum = runClockAlg();
		int offset = 0;
		
		//load in the new data
		while(reader.hasNext()) {
			ram.ram[pfnum][offset] = reader.nextInt();
			offset++;
		}
		
		//System.out.println("loading " +  page + ".pg to ram[" + pfnum + "]");
		return pfnum;
	}
	
	
	int index = 0;
	
	/**
	 * contains page eviction and dirty bit checks
	 * @return
	 * @throws IOException
	 */
	public int runClockAlg() throws IOException {
		//System.out.println("start index: " + index);
		
		int pfnum;
		
		//still have to check if the bit is dirty
		
		while(true) {
			//System.out.println("considering pf# " + pt.pageTable[index].pfnum);
			if(pt.pageTable[index].ref == false) {
				//System.out.println(pt.pageTable[index].pfnum);
				pfnum = pt.pageTable[index].pfnum;
				
				if(index < ptSize - 1) {
					index++;
				} else {
					index = 0;					
				}
				
				
				/**
				 * INEFFICIENT EVICTION
				 */
				for(int i = 0; i < pt.pageTable.length; i++) {
					if(i != index && pt.pageTable[i].pfnum == pfnum && pt.pageTable[i].val == true) {
						pt.pageTable[i].val = false;
						//System.out.println("EVICTING " + i);
						evict = true;
						evictedVP = i;
						/**
						 * CHECKING DIRTY BIT AND WRITING IF IT IS TRUE
						 */
						if(pt.pageTable[i].dir) {
							evictedDirty = 1;
							String vpn_16 = Integer.toHexString(i);
							vpn_16 = vpn_16.toUpperCase();
							//System.out.println("writing to " + vpn_16);
							writeToDisk(i, vpn_16, pt.pageTable[i].pfnum);
						} else {
							evictedDirty = 0;
						}
					}
				}
				
				return pfnum;
			} else {
				//System.out.println(pt.pageTable[index].pfnum);
				pt.pageTable[index].ref = false; //set the 1 to a 0
				
				if(index < ptSize - 1) {
					//System.out.println("a " + pt.pageTable[index].pfnum);
					index++;
				} else {
					index = 0;					
				}
			}			
		}
		
	}
	

	public void setValid(int vpn, boolean val) {
		pt.pageTable[vpn].val = val;
	}
	
	public void setDirty(int vpn, boolean dir) {
		pt.pageTable[vpn].dir = dir;
	}
	
	public void writeToDisk(int vpn, String page, int pfnum) throws IOException {
		if(vpn < 16) {
			page = "0" + page;
		}
		
		FileWriter file = new FileWriter("new_page_files/" + page + ".pg");
		PrintWriter pw = new PrintWriter(file);
		
		for(int i = 0; i < ram.ram[pfnum].length; i++) {
			pw.println(ram.ram[pfnum][i]);
		}
		
		pw.close();
		file.close();
	}
	
	
	public PageTable.PageTableEntry addToPT(int vpnum, int pfnum) {
		//System.out.println("adding " + vpnum + " to the PT");
		
		pt.pageTable[vpnum].val = true;
		pt.pageTable[vpnum].ref = true;
		pt.pageTable[vpnum].dir = false;
		pt.pageTable[vpnum].pfnum = pfnum;
		
		//set val = 0 for all other pte's with the same pfnum
		for(int i = 0; i < pt.pageTable.length; i++) {
			if(i != vpnum && pt.pageTable[i].pfnum == pfnum) {
				pt.pageTable[i].val = false;
			}
		}
		
		return pt.pageTable[vpnum];
	}
	
	
	public void resetAllRef() {
		for(int i = 0; i < pt.pageTable.length; i++) {
			if(pt.pageTable[i].ref == true) {
				pt.pageTable[i].ref = false;				
			}
		}
	}
	
	public int read(int pfnum, int offset) {
		return ram.ram[pfnum][offset];
	}
	
	public void write(int pfnum, int offset, int value) {
		ram.ram[pfnum][offset] = value;
	}
	
	public void printValPages() {
		for(int i = 0; i < pt.pageTable.length; i++) {
			if(pt.pageTable[i].val == true) {
				System.out.println("page " + i + "  pfnum " + pt.pageTable[i].pfnum);
			}
		}
	}
}
