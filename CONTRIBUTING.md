# Contributing to Nebula NatTable

Thanks for your interest in this project. The following information will help you setting up your development environment in order to be able to contribute bugfixes or enhancements to NatTable.

## Project description:

NatTable is a powerful and flexible SWT table/grid widget that is built to handle very large data sets, real-time updates, dynamic styling, and more.
NatTable is a subproject of the Nebula Project, the home of further supplemental custom widgets for SWT.

The project details can be found [here](https://projects.eclipse.org/projects/technology.nebula.nattable).

This project uses [GitHub Issues](https://github.com/eclipse-nattable/nattable/issues) to track ongoing development and issues.
Be sure to search for existing bugs before you create another one. Remember that contributions are always welcome!

Previously this project used Bugzilla. Until the Eclipse Foundation is not shutting down Bugzilla, you can find solved issues there:
* [Search for issues](https://bugs.eclipse.org/bugs/buglist.cgi?product=NatTable)

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team, contributors must
electronically sign the [Eclipse Contributor Agreement (ECA)](https://www.eclipse.org/legal/ECA.php).

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please have a look at the [Eclipse Committer Handbook](https://www.eclipse.org/projects/handbook/#resources-commit)

## Setup and Workflow

The NatTable project basically follows the same process as the Eclipse Platform. As a project hosted on GitHub, we accept pull requests. Please follow the guidelines in the [Eclipse Platform Contribution Guide](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md) if you want to create a pull request for the NatTable project:
* [Setting up Your Eclipse and GitHub Account](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#setting-up-your-eclipse-and-github-account)
* [Recommended Workflow](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#recommended-workflow)
* [Commit Message Recommendations](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#commit-message-recommendations)

## Contact

Contact the project developers via the [project's "dev" mailing list](https://dev.eclipse.org/mailman/listinfo/nattable-dev).

## Environment

The development tools with minimum versions that are used by the NatTable team are:

* JDK 21
* Eclipse 4.32 (2024-06)
* Maven 3.9.8 with Tycho 4.0.13
* Git
* JUnit5

### Source code organization

The NatTable source is divided into the following main projects:

* org.eclipse.nebula.widgets.nattable.core - NatTable Core code
* org.eclipse.nebula.widgets.nattable.core.test - NatTable Core test code
* org.eclipse.nebula.widgets.nattable.extension.e4 - NatTable extensions for Eclipse 4
* org.eclipse.nebula.widgets.nattable.extension.glazedlists - NatTable extensions for GlazedLists
* org.eclipse.nebula.widgets.nattable.extension.glazedlists.test - NatTable extensions for GlazedLists tests
* org.eclipse.nebula.widgets.nattable.extension.nebula - NatTable extensions for Nebula
* org.eclipse.nebula.widgets.nattable.extension.poi - NatTable extensions for Apache POI
* org.eclipse.nebula.widgets.nattable.examples - NatTable example application containing several examples
* org.eclipse.nebula.widgets.nattable.examples.e4 - NatTable examples for Eclipse 4
* org.eclipse.nebula.widgets.nattable.examples.e4.product - NatTable examples application as Eclipse 4 application

In addition there are also various feature projects necessary for release engineering. All of these projects are packaged as Eclipse plugins/OSGi bundles.

## Development IDE Configuration

### Automated Setup

You can set up a pre-configured IDE for the development of the NatTable using the following link:

[![Create Eclipse Development Environment for Eclipse Nebula](https://download.eclipse.org/oomph/www/setups/svg/NatTable.svg)](https://www.eclipse.org/setups/installer/?url=https://raw.githubusercontent.com/eclipse-nattable/nattable/master/setup/NatTableConfiguration.setup&show=true "Click to open Eclipse-Installer Auto Launch or drag into your running installer")

### Tools

Although not required you might want to install m2e together with the Tycho connector in order to be able to build out of the IDE. This step is optional.

### Java Requirements

NatTable Core has a Java 11 and Eclipse Platform 4.16 (2020-06) as minimum requirements, so dependencies to newer Java and platform versions must be avoided.

### Dependencies

After importing the NatTable projects in Eclipse, they will not compile due to missing dependencies. NatTable provides a target platform definition that should be activated in order to resolve the missing dependencies.

* Open the *target-platform* project
* Open the *target-platform.target* file (this may take a while as it downloads the indexes of the p2 repositories the target platform refers to)
* In the resulting editor, click on the *Set as Target Platform* link at the top right (this may also take a while)

After that, the workspace should build cleanly. If not, try *Project > Clean... > All*. If this also doesn't help open *Preferences > Plug-In Development > Target Platform*, select the checked target platform and click *Reload...* this will flush PDE's bundle cache and re-download the artifacts listed in the target platform.

### API Baseline

After importing the NatTable projects you should setup the API baseline if you intend to make changes to the code. This will help you detect the *gravity* of the changes you are making (major, minor, micro) according to [OSGi semantic versioning](http://docs.osgi.org/whitepaper/semantic-versioning/).

The baseline you have to set is provided in the folder *api-baseline*.

Open the workspace preferences (*Window > Preferences*) and head to *Plug-in Development > API Baselines*. There, hit *Add Baseline...* to define a new baseline. Choose any name you like (the version of the release the baseline represents might be suitable, i.e. NatTable 2.0). Then, press *Browse...* and choose the *api-baseline* folder within the NatTable git repository. Press *Finish* and apply the changes. This should trigger a workspace build.

From here on out the API tooling will highlight your code when your changes exceed the current margin given by the snapshot version (e.g. while working on the 1.5 snapshot with the NatTable 1.4 baseline, major API changes will be marked as errors in the IDE). More information about the API tooling can be found [here](https://wiki.eclipse.org/PDE/API_Tools/User_Guide).

Note, that you should aspire to make changes of the magnitude *micro*. These can be released in a Bugfix-Release, while *minor* changes (new APIs) need a more elaborate release process.

### Workspace Preferences

Ensure to set the text file encoding to UTF-8. Otherwise you will get compilation errors on some Strings.

* *Window -> Preferences -> General -> Workspace*
* Set *Text file encoding* to *Other* - *UTF-8*

## Build

The NatTable build is based on pomless Tycho. To build from the command line, you need to execute the following command from the *NATTABLE_TRUNK/nattable* directory:

```
mvn clean verify
```

After the build successfully finished, you will find an Update Site archive in

*NATTABLE_TRUNK/nattable/org.eclipse.nebula.widgets.nattable.updatesite/target*

that you can use for example in a local target definition.
