package repair.handletests.junithandler;

import mainscala.RepairOptions;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Process-based program variant validation
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class JUnitExecutorProcess {

	private Logger log = Logger.getLogger(Thread.currentThread().getName());
	public static String OUTSEP = "mmout";

	public static void main(String[] args){
		System.out.println(System.getProperty("java.home"));
	}

			
	public TestResult execute(URL[] path, String classToExecute,String testExecutorName ,int waitTime) {
		 return execute(urlArrayToString(path), classToExecute,testExecutorName, waitTime);
	}
	
	public TestResult execute(String path, String classToExecute,String testExecutorName ,int waitTime) {
		Process p = null;
	
	
		//if(!ProjectConfiguration.validJDK())
		//	throw new IllegalArgumentException("jdk folder not found, please configure property jvm4testexecution in the configuration.properties file");
		
		String javaPath = System.getProperty("java.home"); //ConfigurationProperties.getProperty("jvm4testexecution");
		javaPath += File.separator + "bin/java";
		String systemcp = System.getProperty("java.class.path");

		String fullPath = systemcp + File.pathSeparator + path;
		//System.out.println(fullPath);
		try {
		
			List<String> command = new ArrayList<String>();
			command.add(javaPath);
			command.add("-cp");
			command.add(fullPath);
			command.add(testExecutorName);//JUnitTestExecutor.class.getName()
			
			command.add(classToExecute);

			ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[command.size()]));
			pb.redirectOutput();
			pb.redirectErrorStream(true);
			long t_start = System.currentTimeMillis();
			p = pb.start();
			String cm2 = command.toString().replace("[", "").replace("]", "").replace(",", " ");
			//log.debug("Executing process: \n"+cm2);
				
			Worker worker = new Worker(p);
			worker.start();
			worker.join(waitTime);
			long t_end = System.currentTimeMillis();
			if(!p.waitFor(RepairOptions.timeout(), TimeUnit.SECONDS)){
				p.destroy();
				return null;
			}
			//worker.interrupt();
			// ---
			int exitvalue = p.exitValue();
			TestResult tr = getTestResultOneLine(p, classToExecute);
			p.destroy();
			//log.debug("Execution time "+((t_end-t_start)/1000)+ " seconds");
			
			return tr;
		} catch (IllegalArgumentException|IOException | InterruptedException ex) {
			System.out.println("The validation thread continues working " + ex.getMessage());
			if (p != null)
				p.destroy();
			throw new RuntimeException("Validation return null");
		}
		
	}

	
	/**
	 * This method analyze the output of the junit executor (i.e.,{@link JUnitTestExecutor}) and return an entity
	 * called TestResult with the result of the test execution 
	 * @param p
	 * @return
	 */
	private TestResult getTestResult(Process p) {
		TestResult tr = new TestResult();
		boolean success = false; String out ="";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			//System.out.println("=====OUTPUTTEST====");
			while ((line = in.readLine()) != null) {
				out+=line+"\n";
				//System.out.println("out="+out);
				if(line.startsWith(OUTSEP)){
					String[] s = line.split(OUTSEP);
					int nrtc = Integer.valueOf(s[1]);
					tr.casesExecuted = nrtc;
					int failing = Integer.valueOf(s[2]);
					tr.failures = failing;
					if(!"".equals(s[3])){
					String[] falinglist = s[3].replace("[", "").replace("]", "").split(",");
					for (String string : falinglist) {
						if(!string.trim().isEmpty())
							tr.failTest.add(string.trim());
					}
					}
					success = true;
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(success)
			return tr;
		else{
		 throw new IllegalArgumentException("The validation process does not execute well the test cases\n "+out);	
		}
	}

	private TestResult getTestResultOneLine(Process p, String testName) {
		TestResult testRes = new TestResult();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			String out = "";
			//System.out.println("=====OUTPUTTEST====");
			ArrayList<String> excutedTest = new ArrayList<>();
			excutedTest.add(testName);
			while ((line = in.readLine()) != null) {
				out += line;
				if(line.contains(SingleJUnitTestRunner.SEPARATOR)) {
					String[] sp=line.split(SingleJUnitTestRunner.SEPARATOR);
					if (sp[1].equals(SingleJUnitTestRunner.SUCCESS)) {
						if(sp[0].contains(SingleJUnitTestRunner.RESULT_PREFIX_PRINT)) {
							testRes.setSuccessTest(excutedTest);
							break;
						}
					} else if (sp[1].equals(SingleJUnitTestRunner.FAILURE)) {
						if(sp[0].contains(SingleJUnitTestRunner.RESULT_PREFIX_PRINT)) {
							testRes.setFailTest(excutedTest);
							break;
						}
					} else {
						//throw new RuntimeException("!!! Handling Invalid Test Output ... output so far => " + out);
					}
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return testRes;
	}

	protected String urlArrayToString(URL[] urls){
		String s= "";
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			s+=url.getPath()+File.pathSeparator;
		}
		return s;
	}

	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}

}
