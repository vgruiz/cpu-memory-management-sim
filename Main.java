/**
 * Author: Victor Ruiz 010698870
 * Instructor: Dominick Atanasio
 * Course: CS 431 - 02
 * Assignment: Project 2
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {

	public static void main(String[] args) throws IOException {
		String page_files = "page_files";
		File originalPages = new File(page_files);
		
		String new_page_files = "new_page_files";
		File newPages = new File(new_page_files);
		
		try {
		    copy(originalPages, newPages);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		String name = args[0].substring(0, 6);
		//File file = new File(args[0]);
		File file = new File("test_files/" + args[0]);
		
		CentralProcessingUnit cpu = new CentralProcessingUnit(/*tlb size*/ 8, /*pt size*/ 256, /*memory address width*/ 12, /*page offset*/ 8);

		cpu.setOutputFileName(name);
		cpu.readTestFile(file);
		cpu.run();
		
	}
	
	public static void copy(File sourceLocation, File targetLocation) throws IOException {
	    if (sourceLocation.isDirectory()) {
	        copyDirectory(sourceLocation, targetLocation);
	    } else {
	        copyFile(sourceLocation, targetLocation);
	    }
	}

	private static void copyDirectory(File source, File target) throws IOException {
	    if (!target.exists()) {
	        target.mkdir();
	    }

	    for (String f : source.list()) {
	        copy(new File(source, f), new File(target, f));
	    }
	}

	private static void copyFile(File source, File target) throws IOException {        
	    try (
	            InputStream in = new FileInputStream(source);
	            OutputStream out = new FileOutputStream(target)
	    ) {
	        byte[] buf = new byte[1024];
	        int length;
	        while ((length = in.read(buf)) > 0) {
	            out.write(buf, 0, length);
	        }
	    }
	}

}
