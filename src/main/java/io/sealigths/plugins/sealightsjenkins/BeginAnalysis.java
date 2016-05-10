package io.sealigths.plugins.sealightsjenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import io.sealigths.plugins.sealightsjenkins.entities.FileBackupInfo;
import io.sealigths.plugins.sealightsjenkins.integration.JarsHelper;
import io.sealigths.plugins.sealightsjenkins.integration.MavenIntegration;
import io.sealigths.plugins.sealightsjenkins.integration.MavenIntegrationInfo;
import io.sealigths.plugins.sealightsjenkins.integration.SeaLightsPluginInfo;
import io.sealigths.plugins.sealightsjenkins.utils.FileAndFolderUtils;
import io.sealigths.plugins.sealightsjenkins.utils.IncludeExcludeFilter;
import io.sealigths.plugins.sealightsjenkins.utils.Logger;
import io.sealigths.plugins.sealightsjenkins.utils.StringUtils;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shahar on 5/9/2016.
 */
@ExportedBean
public class BeginAnalysis extends Builder {

    private final String appName;
    private final String moduleName;
    private final String branch;
    private final boolean enableMultipleBuildFiles;
    private final boolean overrideJars;
    private final boolean multipleBuildFiles;
    private final String pomPath;
    private final String environment;
    private final String packagesIncluded;
    private final String packagesExcluded;
    private final String filesIncluded;
    private final String filesExcluded;
    private final String relativePathToEffectivePom;
    private final boolean recursive;
    private final String workspacepath;
    private final String buildScannerJar;
    private final String testListenerJar;
    private final String apiJar;
    private final String testListenerConfigFile;
    private boolean autoRestoreBuildFile;
    private final String buildFilesPatterns;
    private final String buildFilesFolders;
    private boolean logEnabled;
    private LogDestination logDestination = LogDestination.CONSOLE;
    private final String logFolder;
    private TestingFramework testingFramework = TestingFramework.AUTO_DETECT;
    private LogLevel logLevel = LogLevel.OFF;
    private ProjectType projectType = ProjectType.MAVEN;
    private BuildStrategy buildStrategy = BuildStrategy.ONE_BUILD;

    private final String override_customerId;
    private final String override_url;
    private final String override_proxy;

    @DataBoundConstructor
    public BeginAnalysis(LogLevel logLevel,
                                  String appName, String moduleName, String branch, boolean enableMultipleBuildFiles,
                                  boolean overrideJars, boolean multipleBuildFiles, String pomPath, String environment,
                                  String packagesIncluded, String packagesExcluded, String filesIncluded,
                                  String filesExcluded, String relativePathToEffectivePom, boolean recursive,
                                  String workspacepath, String buildScannerJar, String testListenerJar, String apiJar,
                                  String testListenerConfigFile, boolean autoRestoreBuildFile,
                                  String buildFilesPatterns, String buildFilesFolders,
                                  boolean logEnabled, LogDestination logDestination, String logFolder,
                                  TestingFramework testingFramework, ProjectType projectType, BuildStrategy buildStrategy,
                                  String override_customerId, String override_url, String override_proxy) throws IOException {


        this.override_customerId = override_customerId;
        this.override_url = override_url;
        this.override_proxy = override_proxy;

        this.appName = appName;
        this.moduleName = moduleName;
        this.branch = branch;
        this.pomPath = pomPath;
        this.packagesIncluded = packagesIncluded;
        this.packagesExcluded = packagesExcluded;
        this.filesIncluded = filesIncluded;
        this.filesExcluded = filesExcluded;
        this.relativePathToEffectivePom = relativePathToEffectivePom;
        this.recursive = recursive;
        this.workspacepath = workspacepath;
        this.testListenerConfigFile = testListenerConfigFile;
        this.buildStrategy = buildStrategy;
        this.autoRestoreBuildFile = autoRestoreBuildFile;
        this.environment = environment;
        this.testingFramework = testingFramework;
        this.projectType = projectType;
        this.multipleBuildFiles = multipleBuildFiles;
        this.overrideJars = overrideJars;
        this.buildFilesFolders = buildFilesFolders;
        this.buildFilesPatterns = buildFilesPatterns;
        this.logEnabled = logEnabled;
        this.logLevel = logLevel;
        this.logDestination = logDestination;
        this.logFolder = logFolder;

        this.enableMultipleBuildFiles = enableMultipleBuildFiles;

        if (StringUtils.isNullOrEmpty(buildScannerJar)) {
            //The user didn't specify a specific version of the scanner. Use an embedded one.
            buildScannerJar = JarsHelper.loadJarAndSaveAsTempFile("sl-build-scanner");
        }

        if (StringUtils.isNullOrEmpty(testListenerJar)) {
            //The user didn't specify a specific version of the test listener. Use an embedded one.
            testListenerJar = JarsHelper.loadJarAndSaveAsTempFile("sl-test-listener");
        }

        if (StringUtils.isNullOrEmpty(apiJar)) {
            //The user didn't specify a specific version of the test listener. Use an embedded one.
            apiJar = JarsHelper.loadJarAndSaveAsTempFile("sl-api");
        }

        this.buildScannerJar = buildScannerJar;
        this.testListenerJar = testListenerJar;
        this.apiJar = apiJar;
    }

    @Exported
    public String getAppName() {
        return appName;
    }

    @Exported
    public String getModuleName() {
        return moduleName;
    }

    @Exported
    public String getBranch() {
        return branch;
    }

    @Exported
    public boolean isEnableMultipleBuildFiles() {
        return enableMultipleBuildFiles;
    }

    @Exported
    public boolean isOverrideJars() {
        return overrideJars;
    }

    @Exported
    public boolean isMultipleBuildFiles() {
        return multipleBuildFiles;
    }

    @Exported
    public String getPomPath() {
        return pomPath;
    }

    @Exported
    public String getEnvironment() {
        return environment;
    }

    @Exported
    public String getPackagesIncluded() {
        return packagesIncluded;
    }

    @Exported
    public String getPackagesExcluded() {
        return packagesExcluded;
    }

    @Exported
    public String getFilesIncluded() {
        return filesIncluded;
    }

    @Exported
    public String getFilesExcluded() {
        return filesExcluded;
    }

    @Exported
    public String getRelativePathToEffectivePom() {
        return relativePathToEffectivePom;
    }

    @Exported
    public boolean isRecursive() {
        return recursive;
    }

    @Exported
    public String getWorkspacepath() {
        return workspacepath;
    }

    @Exported
    public String getBuildScannerJar() {
        return buildScannerJar;
    }

    @Exported
    public String getTestListenerJar() {
        return testListenerJar;
    }

    @Exported
    public String getApiJar() {
        return apiJar;
    }

    @Exported
    public String getTestListenerConfigFile() {
        return testListenerConfigFile;
    }

    @Exported
    public boolean isAutoRestoreBuildFile() {
        return autoRestoreBuildFile;
    }

    @Exported
    public void setAutoRestoreBuildFile(boolean autoRestoreBuildFile) {
        this.autoRestoreBuildFile = autoRestoreBuildFile;
    }

    @Exported
    public String getBuildFilesPatterns() {
        return buildFilesPatterns;
    }

    @Exported
    public String getBuildFilesFolders() {
        return buildFilesFolders;
    }

    @Exported
    public boolean isLogEnabled() {
        return logEnabled;
    }

    @Exported
    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    @Exported
    public LogDestination getLogDestination() {
        return logDestination;
    }

    @Exported
    public void setLogDestination(LogDestination logDestination) {
        this.logDestination = logDestination;
    }

    @Exported
    public String getLogFolder() {
        return logFolder;
    }

    @Exported
    public TestingFramework getTestingFramework() {
        return testingFramework;
    }

    @Exported
    public void setTestingFramework(TestingFramework testingFramework) {
        this.testingFramework = testingFramework;
    }

    @Exported
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Exported
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Exported
    public ProjectType getProjectType() {
        return projectType;
    }

    @Exported
    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    @Exported
    public BuildStrategy getBuildStrategy() {
        return buildStrategy;
    }

    @Exported
    public void setBuildStrategy(BuildStrategy buildStrategy) {
        this.buildStrategy = buildStrategy;
    }

    @Exported
    public String getOverride_customerId() {
        return override_customerId;
    }

    @Exported
    public String getOverride_url() {
        return override_url;
    }

    @Exported
    public String getOverride_proxy() {
        return override_proxy;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {

        Logger logger = new Logger(listener.getLogger());
        FilePath ws = build.getWorkspace();
        if (ws == null) {
            return false;
        }

        printFields(logger);

        if (this.autoRestoreBuildFile) {
            tryAddRestoreBuildFilePublisher(build, logger);
        }

        SeaLightsPluginInfo slInfo = createSeaLightsPluginInfo(build, ws, logger);

        configureBuildFilePublisher(build, slInfo.getBuildFilesFolders());

        doMavenIntegration(logger, slInfo);

        return true;
    }

    private void doMavenIntegration(Logger logger, SeaLightsPluginInfo slInfo) throws IOException, InterruptedException {

        List<String> folders = Arrays.asList(slInfo.getBuildFilesFolders().split("\\s*,\\s*"));
        List<FileBackupInfo> pomFiles = getPomFiles(folders, slInfo.getBuildFilesPatterns(), slInfo.isRecursiveOnBuildFilesFolders());

        MavenIntegrationInfo info = new MavenIntegrationInfo(
                pomFiles,
                slInfo,
                testingFramework
        );
        MavenIntegration mavenIntegration = new MavenIntegration(logger, info);
        mavenIntegration.integrate();

    }

    private SeaLightsPluginInfo createSeaLightsPluginInfo(AbstractBuild build, FilePath ws, Logger logger) {

        SeaLightsPluginInfo slInfo = new SeaLightsPluginInfo();
        setGlobalConfiguration(slInfo);

        String workingDir = ws.getRemote();
        String pomPath;
        if (relativePathToEffectivePom != null && !"".equals(relativePathToEffectivePom))
            pomPath = workingDir + "/" + relativePathToEffectivePom;
        else
            pomPath = workingDir + "/pom.xml";

        logger.info("Absolute path to effective file: " + pomPath);

        slInfo.setEnabled(true);
        slInfo.setBuildName(String.valueOf(build.getNumber()));

        if (workspacepath != null && !"".equals(workspacepath))
            slInfo.setWorkspacepath(workspacepath);
        else
            slInfo.setWorkspacepath(workingDir);

        slInfo.setAppName(appName);
        slInfo.setModuleName(moduleName);
        slInfo.setBranchName(branch);
        slInfo.setFilesIncluded(filesIncluded);
        slInfo.setFilesExcluded(filesExcluded);
        slInfo.setRecursive(recursive);
        slInfo.setPackagesIncluded(packagesIncluded);
        slInfo.setPackagesExcluded(packagesExcluded);
        slInfo.setListenerJar(testListenerJar);
        slInfo.setListenerConfigFile(testListenerConfigFile);
        slInfo.setScannerJar(buildScannerJar);
        slInfo.setApiJar(apiJar);
        slInfo.setBuildStrategy(buildStrategy);
        slInfo.setEnvironment(environment);
        slInfo.setLogEnabled(!("Off".equalsIgnoreCase(logLevel.getDisplayName())));
        slInfo.setLogLevel(logLevel);
        slInfo.setLogDestination(logDestination);
        slInfo.setLogFolder(logFolder);

        String foldersToSearch;
        String patternsToSearch;
        if (enableMultipleBuildFiles) {
            foldersToSearch = StringUtils.isNullOrEmpty(buildFilesFolders) ? workingDir : buildFilesFolders;
            patternsToSearch = StringUtils.isNullOrEmpty(buildFilesPatterns) ? "*pom.xml" : buildFilesPatterns;
        } else {
            foldersToSearch = workingDir;
            patternsToSearch = "*pom.xml";
        }

        slInfo.setRecursiveOnBuildFilesFolders(enableMultipleBuildFiles);
        slInfo.setBuildFilesFolders(foldersToSearch);
        slInfo.setBuildFilesPatterns(patternsToSearch);

        return slInfo;
    }

    private void setGlobalConfiguration(SeaLightsPluginInfo slInfo) {

        if (StringUtils.isNullOrEmpty(override_customerId)) {
            slInfo.setCustomerId(getDescriptor().getCustomerId());
        } else {
            slInfo.setCustomerId(override_customerId);
        }

        if (StringUtils.isNullOrEmpty(override_url)) {
            slInfo.setServerUrl(getDescriptor().getUrl());
        } else {
            slInfo.setServerUrl(override_url);
        }

        if (StringUtils.isNullOrEmpty(override_proxy)) {
            slInfo.setProxy(getDescriptor().getProxy());
        } else {
            slInfo.setProxy(override_proxy);
        }

    }

    private List<FileBackupInfo> getPomFiles(List<String> folders, String patterns, boolean recursiveSearch) {
        List<FileBackupInfo> pomFiles = new ArrayList<>();
        IncludeExcludeFilter filter = new IncludeExcludeFilter(patterns, null);

        for (String folder : folders) {
            List<String> matchingPoms = FileAndFolderUtils.findAllFilesWithFilter(folder, recursiveSearch, filter);
            for (String matchingPom : matchingPoms) {
                pomFiles.add(new FileBackupInfo(matchingPom, null));
            }
        }

        return pomFiles;
    }

    private void tryAddRestoreBuildFilePublisher(AbstractBuild build, Logger logger) {
        DescribableList publishersList = build.getProject().getPublishersList();
        boolean found = false;
        for (Object item : publishersList) {
            if (item.toString().contains("RestoreBuildFile")) {
                found = true;
                logger.debug("There was no need to add a new RestoreBuildFile since there is one. Current one:" + item.toString());
                //If found, this was added manually. Remove the check box.
                break;
            }
        }

        if (!found) {
            RestoreBuildFile restoreBuildFile = new RestoreBuildFile(true, buildFilesFolders);
            publishersList.add(restoreBuildFile);
        }
    }

    private void configureBuildFilePublisher(AbstractBuild build, String foldersToSearch) {
        DescribableList publishersList = build.getProject().getPublishersList();
        for (Object item : publishersList) {
            if (item.toString().contains("RestoreBuildFile")) {
                ((RestoreBuildFile) item).setFolders(foldersToSearch);
                return;
            }
        }
    }

    private void printFields(Logger logger) {
        logger.debug("-----------Sealights Jenkins Plugin Configuration--------------");
        logger.debug("Override CustomerId : " + override_customerId);
        logger.debug("Override Url: " + override_url);
        logger.debug("Override proxy:" + override_proxy);
        logger.debug("Testing Framework: " + testingFramework);
        logger.debug("Branch: " + branch);
        logger.debug("App Name:" + appName);
        logger.debug("Module Name:" + moduleName);
        logger.debug("Recursive: " + recursive);
        logger.debug("Workspace: " + workspacepath);
        logger.debug("Environment: " + environment);
        logger.debug("enableMultipleBuildFiles: " + enableMultipleBuildFiles);
        logger.debug("Override Jars: " + overrideJars);
        logger.debug("Multiple Build Files: " + multipleBuildFiles);
        logger.debug("Build Files Folders: " + buildFilesFolders + " buildFilesPatterns: " + buildFilesPatterns);
        logger.debug("Pom Path:" + pomPath);
        logger.debug("Packages Included:" + packagesIncluded);
        logger.debug("Packages Excluded:" + packagesExcluded);
        logger.debug("Files Included:" + filesIncluded);
        logger.debug("Files Excluded:" + filesExcluded);
        logger.debug("Build-Scanner Jar:" + buildScannerJar);
        logger.debug("Test-Listener Jar:" + testListenerJar);
        logger.debug("Test-Listener Configuration File :" + testListenerConfigFile);
        logger.debug("Build Strategy: " + buildStrategy);
        logger.debug("Api Jar:" + apiJar);
        logger.debug("Log Enabled:" + logEnabled);
        logger.debug("Log Destination:" + logDestination);
        logger.debug("Log Level:" + logLevel);
        logger.debug("Log Folder:" + logFolder);
        logger.debug("Auto Restore Build File:" + autoRestoreBuildFile);
        logger.debug("-----------Sealights Jenkins Plugin Configuration--------------");
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        private String customerId;
        private String url;
        private String proxy;

        public DescriptorImpl() {
            super(BeginAnalysis.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            customerId = json.getString("customerId");
            url = json.getString("url");
            proxy = json.getString("proxy");
            save();
            return super.configure(req, json);
        }

        public String getUrl() {
            return url;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getProxy() {
            return proxy;
        }

        public void setProxy(String proxy) {
            this.proxy = proxy;
        }

        public FormValidation doCheckPackagesIncluded(@QueryParameter String packagesIncluded) {
            if (StringUtils.isNullOrEmpty(packagesIncluded))
                return FormValidation.error("Monitored Application Packages is mandatory.");
            return FormValidation.ok();
        }

        public FormValidation doCheckAppName(@QueryParameter String appName) {
            if (StringUtils.isNullOrEmpty(appName))
                return FormValidation.error("App Name is mandatory.");
            return FormValidation.ok();
        }

        public FormValidation doCheckBranch(@QueryParameter String branch) {
            if (StringUtils.isNullOrEmpty(branch))
                return FormValidation.error("Branch Name is mandatory.");
            return FormValidation.ok();
        }

    }
}