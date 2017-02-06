package contest.winter2017;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import securitytests.ArgumentAmountTest;
import securitytests.RandomParameterTest;

/**
 * Exploratory security vulnerability testing is implemented here.
 * 
 */
public class SecurityTester {
	private final ProgramRunner programRunner;
	private final Random random;

	private Set<String> errorMessages = new HashSet<>();
	private List<Output> outputs = null;

	public SecurityTester(ProgramRunner programRunner) {
		this.programRunner = programRunner;
		this.random = new Random(12345L);
	}

	public void runTests(ParameterFactory parameterFactory) throws InterruptedException, ExecutionException {
		List<List<String>> tests = new ArrayList<>();

		// create cases from RandomParameterTest
		final RandomParameterTest randomParameterTest = new RandomParameterTest(random);
		for (int i = 0; i < 20; i++) {
			tests.add(randomParameterTest.getNextInput(parameterFactory));
		}

		// create cases from ArgumentAmountTest
		final ArgumentAmountTest argumentAmountTest = new ArgumentAmountTest(random);
		for (int i = 0; i < 2; i++) {
			tests.add(argumentAmountTest.getNextInput(parameterFactory));
		}

		// run tests
		outputs = programRunner.runTests(tests);
		for (Output output : outputs) {
			String stdErrString = output.getStdErrString();
			if (stdErrString != null && isStdErrExceptional(stdErrString)) {
				errorMessages.add(stdErrString.trim());
			}
		}
	}

	private static boolean isStdErrExceptional(String stdErrString) {
		assert stdErrString != null;
		return stdErrString.startsWith("Exception in");
	}


	public void printInfo(boolean verbose) {
		assert outputs != null;
		if (verbose) {
			for (Output output : outputs) {
				Tester.printBasicTestOutput(output);
			}
		}
	}


	public String getYaml() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unique error count: " + this.errorMessages.size() + "\n");
		if (this.errorMessages.isEmpty()) {
			sb.append("Errors seen: []");
		}
		else {
			sb.append("Errors seen:\n");
			for (String errorString : this.errorMessages) {
				errorString = errorString.trim();
				if (errorString.contains("\n")) {
					sb.append("  - |-\n");
					for (String line : errorString.split("\n")) {
						sb.append("  - " + line + "\n");
					}
				}
				else {
					sb.append("  - " + errorString.trim() + "\n");
				}
			}
		}
		return sb.toString();
	}
}
