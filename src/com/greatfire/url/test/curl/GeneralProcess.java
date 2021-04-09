package com.greatfire.url.test.curl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GeneralProcess {

	private List<String> stdOutput = new ArrayList<String>();
	private List<String> stdError = new ArrayList<String>();
	private List<Exception> exceptions = new ArrayList<Exception>();
	private int exitValue;

	private static Log log = LogFactory.getLog(GeneralProcess.class);

	public GeneralProcess(String[] command) {
		log.info("command: " + StringUtils.join(command, " "));
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			addException(e);
			return;
		}

		final BufferedReader stdInputReader = new BufferedReader(new 
				InputStreamReader(p.getInputStream()));
		BufferedReader stdErrorReader = new BufferedReader(new 
				InputStreamReader(p.getErrorStream()));

		// Get standard output.. for some reason it's done by reading from stdInput
		Thread stdInputThread = new Thread(new Runnable() {

			@Override
			public void run() {
				String s;
				try {
					while ((s = stdInputReader.readLine()) != null) {
						stdOutput.add(s);
					}
				} catch (IOException e) {
					addException(e);
				}
			}
			
		});
		stdInputThread.start();

		// Read standard error. 
		String s;
		try {
			while ((s = stdErrorReader.readLine()) != null) {
				stdError.add(s);
			}
		} catch (IOException e) {
			addException(e);
		}
		
		try {
			stdInputThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			p.waitFor();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			stdInputReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			stdErrorReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			p.getOutputStream().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		exitValue = p.exitValue();
		
		p.destroy();
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}
	
	public int getExitValue() {
		return exitValue;
	}
	
	public List<String> getStdOutput() {
		return stdOutput;
	}
	
	public List<String> getStdError() {
		return stdError;
	}

	private void addException(Exception e) {
		log.error(e, e);
		exceptions.add(e);
	}
}
