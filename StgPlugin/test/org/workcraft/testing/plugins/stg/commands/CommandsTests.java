package org.workcraft.testing.plugins.stg.commands;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.commands.ExplicitPlaceTransformationCommand;
import org.workcraft.plugins.stg.commands.ImplicitPlaceTransformationCommand;
import org.workcraft.plugins.stg.commands.MirrorSignalTransformationCommand;
import org.workcraft.plugins.stg.commands.MirrorTransitionTransformationCommand;
import org.workcraft.plugins.stg.commands.PetriToStgConversionCommand;
import org.workcraft.plugins.stg.commands.StgToPetriConversionCommand;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CommandsTests {

    private static final String[] TEST_STG_WORKS = {
        "org/workcraft/testing/plugins/stg/commands/celement.stg.work",
        "org/workcraft/testing/plugins/stg/commands/buck.stg.work",
        "org/workcraft/testing/plugins/stg/commands/vme.stg.work",
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins(false);
    }

    @Test
    public void testMirrorSignalTransformationCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
            Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
            Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);
            Set<String> srcInternals = srcStg.getSignalNames(Type.INTERNAL, null);

            WorkspaceEntry dstWe = framework.executeCommand(srcWe, MirrorSignalTransformationCommand.class.getSimpleName());
            Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
            Set<String> dstInputs = dstStg.getSignalNames(Type.INPUT, null);
            Set<String> dstOutputs = dstStg.getSignalNames(Type.OUTPUT, null);
            Set<String> dstInternals = dstStg.getSignalNames(Type.INTERNAL, null);

            Assert.assertEquals(srcInputs, dstOutputs);
            Assert.assertEquals(srcOutputs, dstInputs);
            Assert.assertEquals(srcInternals, dstInternals);
        }
    }

    @Test
    public void testMirrorTransitionTransformationCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
            int srcMinusCount = 0;
            int srcPlusCount = 0;
            int srcToggleCount = 0;
            for (SignalTransition srcTransition: srcStg.getSignalTransitions()) {
                switch (srcTransition.getDirection()) {
                case MINUS:
                    srcMinusCount++;
                    break;
                case PLUS:
                    srcPlusCount++;
                    break;
                case TOGGLE:
                    srcToggleCount++;
                    break;
                }
            }

            WorkspaceEntry dstWe = framework.executeCommand(srcWe, MirrorTransitionTransformationCommand.class.getSimpleName());
            Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
            int dstMinusCount = 0;
            int dstPlusCount = 0;
            int dstToggleCount = 0;
            for (SignalTransition dstTransition: dstStg.getSignalTransitions()) {
                switch (dstTransition.getDirection()) {
                case MINUS:
                    dstMinusCount++;
                    break;
                case PLUS:
                    dstPlusCount++;
                    break;
                case TOGGLE:
                    dstToggleCount++;
                    break;
                }
            }

            Assert.assertEquals(srcMinusCount, dstPlusCount);
            Assert.assertEquals(srcPlusCount, dstMinusCount);
            Assert.assertEquals(srcToggleCount, dstToggleCount);
        }
    }

    @Test
    public void testPlaceTransformationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            VisualStg srcStg = WorkspaceUtils.getAs(srcWe, VisualStg.class);
            int srcPlaces = srcStg.getVisualPlaces().size();
            int srcImplicitPlaceArcs = srcStg.getVisualImplicitPlaceArcs().size();
            int srcSignalTransitions = srcStg.getVisualSignalTransitions().size();
            int srcDummyTransitions = srcStg.getVisualDummyTransitions().size();

            WorkspaceEntry expWe = framework.executeCommand(srcWe, ExplicitPlaceTransformationCommand.class.getSimpleName());
            VisualStg expStg = WorkspaceUtils.getAs(expWe, VisualStg.class);
            int expPlaces = expStg.getVisualPlaces().size();
            int expImplicitPlaceArcs = expStg.getVisualImplicitPlaceArcs().size();
            int expSignalTransitions = expStg.getVisualSignalTransitions().size();
            int expDummyTransitions = expStg.getVisualDummyTransitions().size();

            Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, expPlaces + expImplicitPlaceArcs);
            Assert.assertEquals(srcSignalTransitions, expSignalTransitions);
            Assert.assertEquals(srcDummyTransitions, expDummyTransitions);

            WorkspaceEntry impWe = framework.executeCommand(expWe, ImplicitPlaceTransformationCommand.class.getSimpleName());
            VisualStg impStg = WorkspaceUtils.getAs(impWe, VisualStg.class);
            int impPlaces = impStg.getVisualPlaces().size();
            int impImplicitPlaceArcs = expStg.getVisualImplicitPlaceArcs().size();
            int impSignalTransitions = expStg.getVisualSignalTransitions().size();
            int impDummyTransitions = expStg.getVisualDummyTransitions().size();

            Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, impPlaces + impImplicitPlaceArcs);
            Assert.assertEquals(srcSignalTransitions, impSignalTransitions);
            Assert.assertEquals(srcDummyTransitions, impDummyTransitions);
        }
    }

    @Test
    public void testPetriConversionCommands() throws IOException, DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            VisualStg srcStg = WorkspaceUtils.getAs(srcWe, VisualStg.class);
            int srcPlaces = srcStg.getVisualPlaces().size();
            int srcImplicitPlaceArcs = srcStg.getVisualImplicitPlaceArcs().size();
            int srcSignalTransitions = srcStg.getVisualSignalTransitions().size();
            int srcDummyTransitions = srcStg.getVisualDummyTransitions().size();

            WorkspaceEntry midWe = framework.executeCommand(srcWe, StgToPetriConversionCommand.class.getSimpleName());
            VisualPetriNet midPetri = WorkspaceUtils.getAs(midWe, VisualPetriNet.class);
            int midPlaces = midPetri.getVisualPlaces().size();
            int midTransitions = midPetri.getVisualTransitions().size();

            Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, midPlaces);
            Assert.assertEquals(srcSignalTransitions + srcDummyTransitions, midTransitions);

            WorkspaceEntry dstWe = framework.executeCommand(midWe, PetriToStgConversionCommand.class.getSimpleName());
            VisualStg dstStg = WorkspaceUtils.getAs(dstWe, VisualStg.class);
            int dstPlaces = dstStg.getVisualPlaces().size();
            int dstImplicitPlaceArcs = dstStg.getVisualImplicitPlaceArcs().size();
            int dstSignalTransitions = dstStg.getVisualSignalTransitions().size();
            int dstDummyTransitions = dstStg.getVisualDummyTransitions().size();

            Assert.assertEquals(midPlaces, dstPlaces + dstImplicitPlaceArcs);
            Assert.assertEquals(midTransitions, dstDummyTransitions);
            Assert.assertEquals(0, dstSignalTransitions);
        }
    }

}
