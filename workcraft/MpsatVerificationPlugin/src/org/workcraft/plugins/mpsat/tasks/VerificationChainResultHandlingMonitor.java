package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.workspace.WorkspaceEntry;

public class VerificationChainResultHandlingMonitor extends AbstractChainResultHandlingMonitor<VerificationChainOutput, Boolean> {

    public VerificationChainResultHandlingMonitor(WorkspaceEntry we, boolean interactive) {
        super(we, interactive);
    }

    @Override
    public Boolean handleSuccess(Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
        VerificationParameters verificationParameters = chainOutput.getVerificationParameters();

        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();

        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();

        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        VerificationOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();

        return new VerificationOutputInterpreter(getWorkspaceEntry(), exportOutput, pcompOutput,
                mpsatOutput, verificationParameters, chainOutput.getMessage(), isInteractive()).interpret();
    }

    @Override
    public boolean isConsistencyCheckMode(VerificationChainOutput chainOutput) {
        VerificationParameters verificationParameters = chainOutput.getVerificationParameters();
        return (verificationParameters != null)
                && (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);
    }

    @Override
    public Result<? extends VerificationOutput> getFailedMpsatResult(VerificationChainOutput chainOutput) {
        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        if ((mpsatResult != null) && (mpsatResult.getOutcome() == Result.Outcome.FAILURE)) {
            return mpsatResult;
        }
        return null;
    }

}
