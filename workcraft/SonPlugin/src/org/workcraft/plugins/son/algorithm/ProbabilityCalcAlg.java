package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.SyncCycleException;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

import java.util.*;

public class ProbabilityCalcAlg extends RelationAlgorithm {

    private final boolean twoDir;
    // private Condition superIni; NOT NEEDED FOR CALCULATING PROBABILITIES
    private Condition superFinal;

    public ProbabilityCalcAlg(SON net, ScenarioRef s, boolean twoDir)
            throws AlternativeStructureException, SyncCycleException {
        super(net);
        this.twoDir = twoDir;
        // if (!getSyncCPs().isEmpty()) {
        //    throw new SyncCycleException();
        // }
        // TODO Auto-generated constructor stub
    }

    public ProbabilityCalcAlg(SON net) { // REMOVE AFTER TESTING
        super(net);
        this.twoDir = false;
        this.superFinal = null;
    }

    public void estimateEntire() throws TimeEstimationException, TimeOutOfBoundsException {
        // addSuperNodes(); NOT NEEDED FOR CALCULATING PROBABILITIES
        // forwardBFSsonTimes((Time) superIni); NOT NEEDED FOR CALCULATING PROBABILITIES
        // if (twoDir) {
        //    backwardBFSsonTimes((Time) superFinal);
        // }

    }

    // assign specified value from connections to nodes
    public void prepareSuperIni(MathNode n) {
        for (SONConnection con : net.getOutputPNConnections(n)) {
            if (con.getTime().isSpecified()) {
                Node first = con.getFirst();
                if (first instanceof Time) {
                    Interval end = ((Time) first).getEndTime();
                    ((Time) first).setEndTime(Interval.getOverlapping(end, con.getTime()));
                }
            }
        }
    }

    public void CalcBaseLevelONsProbability() {

        BSONAlg bsonAlg = new BSONAlg(net);

        for (ONGroup ON : bsonAlg.getBaseLevelONs(net.getGroups()))
            if (ON != null)
                CalcONProbability(ON);
    }

    /** Calculates the probabilities of the conditions and events of the input ON
     *  breadth-first to minimise computational complexity.
     **/
    private void CalcONProbability(ONGroup ON) {

        Collection<Condition>   statesSet = new ArrayList<>(); // Conditions to be processed breadth-first.
        Collection<Event>       eventsSet = new ArrayList<>(); // Events to be processed breadth-first.
        Map<Node, Integer>      visitsMap = new HashMap<>(); // number of visits to nodes during the calculation

        for (Condition state : ON.getConditions())
            visitsMap.put(state, 0);

        for (Event event : ON.getEvents())
            visitsMap.put(event, 0);

        statesSet = getONInitial(ON);
        for (Condition state : statesSet)
            state.setProbability(1.0);

        while (!statesSet.isEmpty()) {
            CalcEventsProbability(statesSet, eventsSet, visitsMap);
            statesSet.clear();
            CalcStatesProbability(eventsSet, statesSet, visitsMap);
            eventsSet.clear();
        }
    }

    /** Calculates the probabilities of the set of events caused directly by the input set of conditions
     *  breadth-first, allowing for convergent paths of unequal length.
     **/
    private void CalcEventsProbability(Collection<Condition> stateset, Collection<Event> eventset, Map<Node, Integer> visitsmap) {

        Collection<Event>   statePostEventsSet = new ArrayList<>(); // Events caused directly by a condition.

        for (Condition state : stateset) {
            for (Node node : getPostPNSet(state))
                statePostEventsSet.add((Event) node);

            if (statePostEventsSet.size() > 0) {
                Event ev = statePostEventsSet.iterator().next();
                if (visitsmap.get(ev) == 0) {
                    if (statePostEventsSet.size() == 1) {
                        ev.setProbability(state.getProbability());
                    }
                    else {
                        int sumofeventsweight = 0;

                        for (Event event : statePostEventsSet)
                            sumofeventsweight += event.getWeight();

                        for (Event event : statePostEventsSet)
                            event.setProbability(state.getProbability() * (double) event.getWeight() / (double) sumofeventsweight);
                    }
                }
                for (Event event : statePostEventsSet) {
                    visitsmap.put(event, visitsmap.get(event) + 1);
                    if (visitsmap.get(event) == getPrePNSet(event).size())
                        eventset.add(event);
                }
                statePostEventsSet.clear();
            }
        }
    }

    /** Calculates the probabilities of the set of conditions caused directly by the input set of events
     *  breadth-first, allowing for convergent paths of unequal length.
     **/
    private void CalcStatesProbability(Collection<Event> eventset, Collection<Condition> stateset, Map<Node, Integer> visitsmap) {

        Collection<Condition>   eventPostStatesSet = new ArrayList<>(); // Conditions caused directly by an event.

        for (Event event : eventset) {
            for (Node node : getPostPNSet(event))
                eventPostStatesSet.add((Condition) node);

            if (eventPostStatesSet.size() > 0) {
                Condition st = eventPostStatesSet.iterator().next();
                if (visitsmap.get(st) == 0) {
                    for (Condition state : eventPostStatesSet)
                        state.setProbability(event.getProbability());
                }
                else if (visitsmap.get(st) < getPrePNSet(st).size()) {
                    for (Condition state : eventPostStatesSet)
                        state.setProbability(state.getProbability() + event.getProbability());
                }
                for (Condition state : eventPostStatesSet) {
                    visitsmap.put(state, visitsmap.get(state) + 1);
                    if (visitsmap.get(state) == getPrePNSet(state).size())
                        stateset.add(state);
                }
                eventPostStatesSet.clear();
            }
        }
    }

    /** Calculates the probability of a node (Condition or Event) breadth-first,
     *  allowing for convergent paths of unequal length.
     **/
    public double CalcNodeProbability(Node node) {

        ONGroup ON;

        ON = net.getGroup(node);
        if (ON == null)
            return -1;
        else {
            Collection<Condition>   statesSet = new ArrayList<>(); // Conditions to be processed breadth-first.
            Collection<Event>       eventsSet = new ArrayList<>(); // Events to be processed breadth-first.
            Map<Node, Integer>      visitsMap = new HashMap<>(); // number of visits to nodes during the calculation

            for (Condition state : ON.getConditions())
                visitsMap.put(state, 0);

            for (Event event : ON.getEvents())
                visitsMap.put(event, 0);

            statesSet = getONInitial(ON);
            for (Condition state : statesSet)
                state.setProbability(1.0);

            if (statesSet.contains(node))
                return ((Condition) node).getProbability();

            while (!statesSet.isEmpty()) {
                CalcEventsProbability(statesSet, eventsSet, visitsMap);
                if (eventsSet.contains(node))
                    return ((Event) node).getProbability();

                statesSet.clear();
                CalcStatesProbability(eventsSet, statesSet, visitsMap);
                if (statesSet.contains(node))
                    return ((Condition) node).getProbability();

                eventsSet.clear();
            }
            return -1; // Input node (Condition or Event) not found.
        }
    }

}
