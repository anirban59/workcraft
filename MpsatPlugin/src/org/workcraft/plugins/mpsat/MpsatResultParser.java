package org.workcraft.plugins.mpsat;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Trace;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;

public class MpsatResultParser {
	private String mpsatOutput;
	private LinkedList<Trace> solutions;

	public MpsatResultParser(ExternalProcessResult result) {
		try {
			mpsatOutput = new String(result.getOutput(), "ISO-8859-1"); // iso-latin-1
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		solutions = new LinkedList<Trace>();
		Pattern solution = Pattern.compile("SOLUTION.*\n(.*?)\n", Pattern.UNIX_LINES);
		Matcher matcher = solution.matcher(mpsatOutput);
		while (matcher.find()) {
			Trace trace = new Trace();
			String mpsatTrace = matcher.group(1);
			if (!mpsatTrace.isEmpty()) {
				String[] mpsatFlatTransitions = mpsatTrace.replaceAll("\\s","").split(",");
				for (String mpsatFlatTransition: mpsatFlatTransitions) {
					String mpsatTransition = mpsatFlatTransition.replace(NamespaceHelper.flatNameSeparator, NamespaceHelper.hierarchySeparator);
					String transition = mpsatTransition.substring(mpsatTransition.indexOf('.') + 1);
					trace.add(transition);
				}
			}
			solutions.add(trace);
		}
	}

	public List<Trace> getSolutions() {
		return Collections.unmodifiableList(solutions);
	}
}
