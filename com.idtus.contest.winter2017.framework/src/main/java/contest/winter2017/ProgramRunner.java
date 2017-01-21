package contest.winter2017;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;

import contest.winter2017.Tester.TesterOptions;

public class ProgramRunner {
	private final String jarToTestPath;
	private final String jacocoAgentJarPath;
	private final String jacocoOutputFilePath;
	private final boolean printDebug;

	public ProgramRunner(TesterOptions options) {
		this.jarToTestPath = options.jarToTestPath;
		this.jacocoAgentJarPath = options.jacocoAgentJarPath;
		this.jacocoOutputFilePath = options.jacocoOutputFilePath;
		this.printDebug = options.verbose && !options.yamlOnly;
	}

	/**
	 * This method will instrument and execute the jar under test with the supplied parameters.
	 * This method should be used for both basic tests and security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on the command line
	 * because the user has already set the appropriate environment variable path. 
	 * 
	 * @param parameters - array of Objects that represents the parameter values to use for this 
	 *                     execution of the jar under test
	 * 
	 * @return Output representation of the standard out and standard error associated with the run
	 */
	public Output instrumentAndExecuteCode(Object[] parameters) {
		// we are building up a command line statement that will use java -jar to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage metrics
		List<String> command = new ArrayList<String>();
		command.add("java");
		command.add("-javaagent:" + jacocoAgentJarPath + "=destfile=" + jacocoOutputFilePath);
		command.add("-jar");
		command.add(this.jarToTestPath);
		for (Object o : parameters) {
			command.add(o.toString());
		}

		// show the user the command to run and prepare the process using the command
		if (printDebug) {
			System.out.println("command to run: " + command);
		}

		ProcessBuilder pb = new ProcessBuilder(command);
		String stdOutString;
		String stdErrString;

		// read stdout and stderr in separate threads to avoid blocking
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Process process = pb.start();
			InputStream isOut = process.getInputStream();
			InputStream isErr = process.getErrorStream();

			// add tasks to executor
			Future<String> futureOut = executor.submit(new InputStringCollector(isOut));
			Future<String> futureErr = executor.submit(new InputStringCollector(isErr));

			// await completion
			process.waitFor();
			stdOutString = futureOut.get();
			stdErrString = futureErr.get();
			executor.shutdownNow();
		}
		catch (IOException | ExecutionException | InterruptedException e) {
			System.out.println("ERROR: IOException has prevented execution of the command: " + command);
			return null;
		}

		// we now have the output as an object from the run of the black-box jar
		// this output object contains both the standard output and the standard error
		return new Output(stdOutString, stdErrString);
	}

	private static class InputStringCollector implements Callable<String> {
		InputStream in;

		public InputStringCollector(InputStream inputStream) {
			in = inputStream;
		}

		@Override
		public String call() throws IOException {
			return IOUtils.toString(in, Charset.defaultCharset());
		}
	}
}