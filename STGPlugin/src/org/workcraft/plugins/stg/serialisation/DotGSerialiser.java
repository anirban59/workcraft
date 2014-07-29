/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.stg.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

public class DotGSerialiser implements ModelSerialiser {
	class ReferenceResolver implements ReferenceProducer {
		HashMap<Object, String> refMap = new HashMap<Object, String>();

		public String getReference(Object obj) {
			return refMap.get(obj);
		}
	}

	private void writeSignalsHeader (PrintWriter out, Collection<String> signalNames, String header) {
		if (signalNames.isEmpty())
			return;

		LinkedList<String> sortedNames = new LinkedList<String>(signalNames);
		Collections.sort(sortedNames);

		out.print(header);

		for (String s : sortedNames) {
			out.print(" ");
			out.print(NamespaceHelper.getFlatName(s));
		}

		out.print("\n");
	}

	private Iterable<Node> sortNodes (Collection<? extends Node> nodes, final Model model) {
		ArrayList<Node> list = new ArrayList<Node>(nodes);

		Collections.sort(list, new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				return model.getNodeReference(o1).compareTo(model.getNodeReference(o2));
			}
		});

		return list;
	}

	private void writeGraphEntry(PrintWriter out, Model model, Node node) {
		if (node instanceof STGPlace)
			if (((STGPlace)node).isImplicit())
				return;

		if (model.getPostset(node).size()>0) {
			out.write(NamespaceHelper.getFlatName(model.getNodeReference(node)));

			for (Node n : sortNodes (model.getPostset(node), model)  ) {
				if (n instanceof STGPlace) {
					if (((STGPlace)n).isImplicit()) {
						Collection<Node> postset = model.getPostset(n);
						if (postset.size() > 1)
							throw new FormatException("Implicit place cannot have more than one node in postset");
						out.write(" " + NamespaceHelper.getFlatName(model.getNodeReference(postset.iterator().next())));
					} else
						out.write(" " + NamespaceHelper.getFlatName(model.getNodeReference(n)));
				} else {
					out.write(" " + NamespaceHelper.getFlatName(model.getNodeReference(n)));
				}
			}

			out.write("\n");
		}
	}

	public ReferenceProducer serialise(Model model, OutputStream outStream, ReferenceProducer inRef) {
		PrintWriter out = new PrintWriter(outStream);
		out.print("# STG file generated by Workcraft -- http://workcraft.org\n");

		ReferenceResolver resolver = new ReferenceResolver();

		if (model instanceof STGModel)
			writeSTG((STGModel)model, out);
		else if (model instanceof PetriNetModel)
			writePN((PetriNetModel)model, out);
		else
			throw new ArgumentException ("Model class not supported: " + model.getClass().getName());

		out.print(".end\n");

		out.close();

		return resolver;
	}

	private void writeSTG(STGModel stg, PrintWriter out) {
		writeSignalsHeader(out, stg.getSignalReferences(Type.INTERNAL), ".internal");
		writeSignalsHeader(out, stg.getSignalReferences(Type.INPUT), ".inputs");
		writeSignalsHeader(out, stg.getSignalReferences(Type.OUTPUT), ".outputs");
		writeSignalsHeader(out, stg.getDummyReferences(), ".dummy");

		out.print(".graph\n");

		//out.println("# Signal transitions");
		for (Node n : sortNodes (stg.getSignalTransitions(), stg))
			writeGraphEntry (out, stg, n);
		//out.println();

		//out.print("# Dummy transitions");
		for (Node n : sortNodes (stg.getDummyTransitions(), stg))
			writeGraphEntry (out, stg, n);
		//out.println();

		//out.print("# Places")
		for (Node n : sortNodes (stg.getPlaces(), stg))
			writeGraphEntry (out, stg, n);
		//out.println();

		writeMarking(stg, stg.getPlaces(), out);
	}

	private void writeMarking(Model model, Collection<Place> places, PrintWriter out) {
		ArrayList<String> markingEntries = new ArrayList<String>();

		for (Place p: places) {
			final int tokens = p.getTokens();
			final String reference;

			if (p instanceof STGPlace) {
				if ( ((STGPlace)p).isImplicit() ) {
					reference = "<" + NamespaceHelper.getFlatName(model.getNodeReference(model.getPreset(p).iterator().next())) + "," +
							NamespaceHelper.getFlatName(model.getNodeReference(model.getPostset(p).iterator().next())) + ">";
				} else
					reference = NamespaceHelper.getFlatName(model.getNodeReference(p));
			} else {
				reference = NamespaceHelper.getFlatName(model.getNodeReference(p));
			}

			if (tokens == 1)
				markingEntries.add(reference);
			else if (tokens > 1)
				markingEntries.add(reference + "=" + tokens);
		}

		Collections.sort(markingEntries);

		out.print(".marking {");

		boolean first = true;

		for (String m : markingEntries) {
			if (!first) {
				out.print (" ");
			} else
				first = false;

			out.print (m);
		}

		out.print ("}\n");

		StringBuilder capacity = new StringBuilder();

		for (Place p : places) {
			if (p instanceof STGPlace)
				if (((STGPlace)p).getCapacity() != 1)
				capacity.append(" " + NamespaceHelper.getFlatName(model.getNodeReference(p)) + "=" + ((STGPlace)p).getCapacity());
		}

		if (capacity.length() > 0)
			out.print(".capacity" + capacity + "\n");
	}

	private void writePN(PetriNetModel net, PrintWriter out) {
		LinkedList<String> transitions = new LinkedList<String>();

		for (Transition t : net.getTransitions())
			transitions.add(NamespaceHelper.getFlatName(net.getNodeReference(t)));

		writeSignalsHeader(out, transitions, ".dummy");

		out.print(".graph\n");

		for (Transition t : net.getTransitions())
			writeGraphEntry (out, net, t);

		for (Place p : net.getPlaces())
			writeGraphEntry (out,net, p);

		writeMarking(net, net.getPlaces(), out);
	}

	public boolean isApplicableTo(Model model) {
		if (model instanceof STGModel || model instanceof PetriNetModel)
			return true;
		return false;
	}

	public String getDescription() {
		return "Workcraft STG serialiser";
	}

	public String getExtension() {
		return ".g";
	}

	public UUID getFormatUUID() {
		return Format.STG;
	}
}