package io.sealights.plugins.sealightsjenkins.buildsteps.cli.configurationtechnologies;
/*
* This class represents the 'DotNet' item in 'TechnologyOptions' DropDown under 'Config' option.
 * currently not in used. the 'extends' to hide it from ui.
* */
import hudson.Extension;
import hudson.model.AbstractProject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class DotNetOptions /*extends TechnologyOptions*/ {
    private String namespacesIncluded;
    private String namespacesExcluded;

    @DataBoundConstructor
    public DotNetOptions(String namespacesIncluded, String namespacesExcluded) {
        this.namespacesIncluded = namespacesIncluded;
        this.namespacesExcluded = namespacesExcluded;
    }

    @Exported
    public String getNamespacesIncluded() {
        return namespacesIncluded;
    }

    @Exported
    public void setNamespacesIncluded(String namespacesIncluded) {
        this.namespacesIncluded = namespacesIncluded;
    }

    @Exported
    public String getNamespacesExcluded() {
        return namespacesExcluded;
    }

    @Exported
    public void setNamespacesExcluded(String namespacesExcluded) {
        this.namespacesExcluded = namespacesExcluded;
    }

    @Extension
    public static class DescriptorImpl extends TechnologyOptionsDescriptor {
        @Override
        public String getDisplayName() {
            return "DotNet options";
        }
    }
}
