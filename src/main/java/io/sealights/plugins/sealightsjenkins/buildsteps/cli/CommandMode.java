package io.sealights.plugins.sealightsjenkins.buildsteps.cli;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Saveable;
import hudson.util.DescribableList;
import io.sealights.plugins.sealightsjenkins.BeginAnalysis;
import io.sealights.plugins.sealightsjenkins.buildsteps.cli.configurationtechnologies.JavaOptions;
import io.sealights.plugins.sealightsjenkins.buildsteps.cli.configurationtechnologies.TechnologyOptions;
import io.sealights.plugins.sealightsjenkins.buildsteps.cli.configurationtechnologies.TechnologyOptionsDescriptor;
import io.sealights.plugins.sealightsjenkins.buildsteps.cli.entities.CommandModes;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.io.Serializable;
import java.util.List;

/**
 * This class holds the different command options and their arguments in the UI
 */
public class CommandMode implements Describable<CommandMode>, ExtensionPoint, Serializable {

    private final CommandModes currentMode;
    private  String buildSessionId;
    private  String additionalArguments;

    private CommandMode(final CommandModes currentMode, final String buildSessionId, final String additionalArguments) {
        this.currentMode = currentMode;
        this.buildSessionId = buildSessionId;
        this.additionalArguments = additionalArguments;
    }

    @Exported
    public void setBuildSessionId(String buildSessionId) {
        this.buildSessionId = buildSessionId;
    }

    @Exported
    public void setAdditionalArguments(String additionalArguments) {
        this.additionalArguments = additionalArguments;
    }

    @Exported
    public String getBuildSessionId() {
        return buildSessionId;
    }

    @Exported
    public String getAdditionalArguments() {
        return additionalArguments;
    }

    @Exported
    public CommandModes getCurrentMode() {
        return currentMode;
    }

    @Override
    public Descriptor<CommandMode> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public static class CommandModeDescriptor extends Descriptor<CommandMode> {

        private String selectedMode;

        protected CommandModeDescriptor(final Class<? extends CommandMode> clazz, final String selectedMode) {
            super(clazz);
            this.selectedMode = selectedMode;
        }

        public boolean isDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return selectedMode;
        }

        public DescriptorExtensionList<CommandMode, CommandModeDescriptor> getRepositoryLocationDescriptors() {
            return Hudson.getInstance().getDescriptorList(CommandMode.class);
        }
    }

    public static class StartView extends CommandMode {

        private String testStage;

        @DataBoundConstructor
        public StartView(String testStage, String buildSessionId, String additionalArguments) {
            super(CommandModes.Start, buildSessionId, additionalArguments);
            this.testStage = testStage;
        }

        public String getTestStage() {
            return testStage;
        }

        public void setTestStage(String testStage) {
            this.testStage = testStage;
        }

        @Extension
        public static class StartDescriptor extends CommandModeDescriptor {

            @Override
            public boolean isDefault() {
                return false;
            }

            public StartDescriptor() {
                super(StartView.class, CommandModes.Start.getDisplayName());
            }
        }

    }

    public static class EndView extends CommandMode {

        @DataBoundConstructor
        public EndView(String buildSessionId, String additionalArguments) {
            super(CommandModes.End, buildSessionId, additionalArguments);
        }

        @Extension
        public static class EndDescriptor extends CommandModeDescriptor {
            public EndDescriptor() {
                super(EndView.class, CommandModes.End.getDisplayName());
            }
        }

    }

    public static class UploadReportsView extends CommandMode {

        private String reportFiles;
        private String reportsFolders;
        private boolean hasMoreRequests;
        private String source;

        @DataBoundConstructor
        public UploadReportsView(String reportFiles, String reportsFolders,
                                 String source, String buildSessionId, String additionalArguments) {
            super(CommandModes.UploadReports, buildSessionId, additionalArguments);
            this.reportFiles = reportFiles;
            this.reportsFolders = reportsFolders;
            this.hasMoreRequests = true;
            this.source = source;
        }

        public String getReportFiles() {
            return reportFiles;
        }

        public void setReportFiles(String reportFiles) {
            this.reportFiles = reportFiles;
        }

        public String getReportsFolders() {
            return reportsFolders;
        }

        public void setReportsFolders(String reportsFolders) {
            this.reportsFolders = reportsFolders;
        }

        public boolean getHasMoreRequests() {
            return hasMoreRequests;
        }

        public void setHasMoreRequests(boolean hasMoreRequests) {
            this.hasMoreRequests = hasMoreRequests;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        @Extension
        public static class UploadReportsDescriptor extends CommandModeDescriptor {
            public UploadReportsDescriptor() {
                super(UploadReportsView.class, CommandModes.UploadReports.getDisplayName());
            }
        }

    }

    public static class ExternalReportView extends CommandMode {

        private String report;

        public String getReport() {
            return report;
        }

        public void setReport(String report) {
            this.report = report;
        }

        @DataBoundConstructor
        public ExternalReportView(String report, String buildSessionId, String additionalArguments) {
            super(CommandModes.ExternalReport, buildSessionId, additionalArguments);
            this.report = report;
        }

        @Extension
        public static class ExternalReportDescriptor extends CommandModeDescriptor {
            public ExternalReportDescriptor() {
                super(ExternalReportView.class, CommandModes.ExternalReport.getDisplayName());
            }
        }

    }

    public static class ConfigView extends CommandMode {

        private DescribableList<TechnologyOptions, TechnologyOptionsDescriptor> techOptions;
        private List<BeginAnalysis> branches;
        private String appName;
        private String branchName;
        private CommandBuildName buildName;
        private transient String packagesIncluded;
        private transient String packagesExcluded;
        private String labId;

        public List<BeginAnalysis> getBranches() {
            return branches;
        }

        public void setBranches(List<BeginAnalysis> branches) {
            this.branches = branches;
        }

        @DataBoundConstructor
        public ConfigView(String appName, String branchName,
                          CommandBuildName buildName, String labId, String buildSessionId, String additionalArguments,
                          List<TechnologyOptions> techOptions) {
                          this(appName, branchName, buildName, labId, buildSessionId, additionalArguments,
                           techOptions, CommandModes.Config);
        }

        /**
         * Extra constructor used in sub class PrConfig
         * @param appName
         * @param branchName
         * @param buildName
         * @param labId
         * @param buildSessionId
         * @param additionalArguments
         * @param techOptions
         * @param currentMode
         */
        private ConfigView(String appName, String branchName,
                          CommandBuildName buildName, String labId, String buildSessionId, String additionalArguments,
                          List<TechnologyOptions> techOptions, CommandModes currentMode) {
            super(currentMode, buildSessionId, additionalArguments);
            this.appName = appName;
            this.branchName = branchName;
            this.buildName = buildName;
            this.labId = labId;
            this.techOptions = new DescribableList<>(this.getDescriptor(), techOptions);
        }

        public DescribableList<TechnologyOptions, TechnologyOptionsDescriptor> getTechOptions() {
            return techOptions;
        }

        public void setTechOptions(DescribableList<TechnologyOptions, TechnologyOptionsDescriptor> techOptions) {
            this.techOptions = techOptions;
        }

        @Exported
        public String getAppName() {
            return appName;
        }

        @Exported
        public void setAppName(String appName) {
            this.appName = appName;
        }

        @Exported
        public String getBranchName() {
            return branchName;
        }

        @Exported
        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        @Exported
        public CommandBuildName getBuildName() {
            return buildName;
        }

        @Exported
        public void setBuildName(CommandBuildName buildName) {
            this.buildName = buildName;
        }

        @Exported
        public String getLabId() {
            return labId;
        }

        @Exported
        public void setLabId(String labId) {
            this.labId = labId;
        }

        @Exported
        public String getPackagesIncluded() {
            return packagesIncluded;
        }

        @Exported
        public void setPackagesIncluded(String packagesIncluded) {
            this.packagesIncluded = packagesIncluded;
        }

        public String getPackagesExcluded() {
            return packagesExcluded;
        }

        public void setPackagesExcluded(String packagesExcluded) {
            this.packagesExcluded = packagesExcluded;
        }

        @Extension
        public static class ConfigDescriptor extends CommandModeDescriptor {

            @Override
            public boolean isDefault() {
                return true;
            }

            public ConfigDescriptor() {
                super(ConfigView.class, CommandModes.Config.getDisplayName());
            }

            public ConfigDescriptor(Class<? extends CommandMode> clazz, final String selectedMode) {
                super(clazz, selectedMode);
            }

            public DescriptorExtensionList<TechnologyOptions, TechnologyOptionsDescriptor> getTechnologiesDescriptors() {
                return TechnologyOptionsDescriptor.all();
            }

            public DescriptorExtensionList<CommandBuildName, CommandBuildName.CommandBuildNameDescriptor> getBuildNameDescriptorList() {
                return Jenkins.getInstance().getDescriptorList(CommandBuildName.class);
            }
        }
    }

    public static class PrConfigView extends ConfigView {
        private String latestCommit;
        private String pullRequestNumber;
        private String repoUrl;
        private String targetBranch;

        @DataBoundConstructor
        public PrConfigView(String appName, String branchName, CommandBuildName buildName, String labId,
         String buildSessionId, String additionalArguments, List<TechnologyOptions> techOptions, String latestCommit,
          String pullRequestNumber, String repoUrl, String targetBranch) {
            super(appName, branchName, buildName, labId, buildSessionId, additionalArguments, techOptions,
             CommandModes.PrConfig);
            this.latestCommit = latestCommit;
            this.pullRequestNumber = pullRequestNumber;
            this.repoUrl = repoUrl;
            this.targetBranch = targetBranch;
        }

        @Exported
        public String getLatestCommit() {
            return latestCommit;
        }

        @Exported
        public void setLatestCommit(String latestCommit) {
            this.latestCommit = latestCommit;
        }

        @Exported
        public String getPullRequestNumber() {
            return pullRequestNumber;
        }

        @Exported
        public void setPullRequestNumber(String pullRequestNumber) {
            this.pullRequestNumber = pullRequestNumber;
        }

        @Exported
        public String getRepoUrl() {
            return repoUrl;
        }

        @Exported
        public void setRepoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
        }

        @Exported
        public String getTargetBranch() {
            return targetBranch;
        }

        @Exported
        public void setTargetBranch(String targetBranch) {
            this.targetBranch = targetBranch;
        }

        @Extension
        public static class PrConfigDescriptor extends ConfigDescriptor {
            public boolean isDefault() {
                return false;
            }
            public PrConfigDescriptor() {
                super(PrConfigView.class, CommandModes.PrConfig.getDisplayName());
            }
        }
    }


}
