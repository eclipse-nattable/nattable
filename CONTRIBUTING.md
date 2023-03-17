# Contributing to Nebula NatTable

Thanks for your interest in this project. The following information will help you setting up your development environment in order to be able to contribute bugfixes or enhancements to NatTable.

## Project description:

NatTable is a powerful and flexible SWT table/grid widget that is built to handle very large data sets, real-time updates, dynamic styling, and more.
NatTable is a subproject of the Nebula Project, the home of further supplemental custom widgets for SWT.

The project details can be found [here](https://projects.eclipse.org/projects/technology.nebula.nattable).

This project uses Bugzilla to track ongoing development and issues.

* [Search for issues](https://bugs.eclipse.org/bugs/buglist.cgi?product=NatTable)
* [Report a new issue](https://bugs.eclipse.org/bugs/enter_bug.cgi?product=NatTable)

Be sure to search for existing bugs before you create another one. Remember that
contributions are always welcome!

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project team, contributors must
electronically sign the [Eclipse Contributor Agreement (ECA)](https://www.eclipse.org/legal/ECA.php).

Commits that are provided by non-committers must have a Signed-off-by field in
the footer indicating that the author is aware of the terms by which the
contribution has been provided to the project. The non-committer must
additionally have an Eclipse Foundation account and must have a signed Eclipse
Contributor Agreement (ECA) on file.

For more information, please have a look at the [Eclipse Committer Handbook](https://www.eclipse.org/projects/handbook/#resources-commit)

## Contact

Contact the project developers via the [project's "dev" list](https://dev.eclipse.org/mailman/listinfo/nattable-dev).

## Environment

The development tools with minimum versions that are used by the NatTable team are:

* JDK 17
* Eclipse 4.26 (2022-12)
* Maven 3.8.6 with Tycho 3.0.3
* Git
* Gerrit
* JUnit5

## Gerrit server configuration

To be able to contribute to the project, patches need to be provided via Gerrit. You therefore need a Gerrit account and configure the authentication there. This is explained for example [here](https://www.vogella.com/tutorials/EclipsePlatformDevelopment/article.html#exercise-setup-user-account).

## Obtaining sources and importing projects into Eclipse

The NatTable sources are hosted in Git. To get the sources you need to clone the repository either via command line or via EGit Eclipse integration.

### Cloning via command line

Change to the directory where you want to store the local working copy of the NatTable repository (NATTABLE_REPO).

Clone the repository to that directory by executing the following Git command:

```
git clone ssh://<user_id>@git.eclipse.org:29418/nattable/org.eclipse.nebula.widgets.nattable
```

After that, import the projects into Eclipse

* File -> Import
* General -> Existing Projects into Workspace
* Next
* Select root directory: (NATTABLE_REPO/org.eclipse.nebula.widgets.nattable)
* Finish

### Cloning via EGit

First, verify that the default repository folder as set on the main Git preference page is to your liking.

Then, clone the repository and import the projects:

* File -> Import
* Git -> Projects from Git
* Select Clone URI
* Enter the URI  
`ssh://<user_id>@git.eclipse.org:29418/nattable/org.eclipse.nebula.widgets.nattable`
* Import existing projects into the workspace from the newly created working directory

### Source code organization

The NatTable source is divided into the following main projects:

* org.eclipse.nebula.widgets.nattable.core - NatTable Core code
* org.eclipse.nebula.widgets.nattable.core.test - NatTable Core test code
* org.eclipse.nebula.widgets.nattable.extension.e4 - NatTable extensions for Eclipse 4
* org.eclipse.nebula.widgets.nattable.extension.glazedlists - NatTable extensions for GlazedLists
* org.eclipse.nebula.widgets.nattable.extension.glazedlists.test - NatTable extensions for GlazedLists tests
* org.eclipse.nebula.widgets.nattable.extension.nebula - NatTable extensions for Nebula
* org.eclipse.nebula.widgets.nattable.extension.poi - NatTable extensions for Apache POI
* org.eclipse.nebula.widgets.examples - NatTable example application containing several examples
* org.eclipse.nebula.widgets.examples.e4 - NatTable examples for Eclipse 4
* org.eclipse.nebula.widgets.examples.e4.product - NatTable examples application as Eclipse 4 application

In addition there are also various feature projects necessary for release engineering. All of these projects are packaged as Eclipse plugins/OSGi bundles.

## Development IDE Configuration

### Tools

Although not required you might want to install m2e together with the Tycho connector in order to be able to build out of the IDE. This step is optional.

### Java Requirements

NatTable Core has a Java 8 and Eclipse Platform 3.5 (Galileo) as minimum requirements, so dependencies to newer Java and platform versions must be avoided. The Nebula extension and the E4 extension have a minimum dependency to Java 8 and Eclipse Neon.

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

### Contributing Patches

We use Gerrit for reviewing and accepting patches. Please have a look at the [Eclipse Gerrit Guide](https://wiki.eclipse.org/Gerrit#Doing_Code_Reviews_with_Gerrit).

* Make small logical changes.
* Provide a meaningful commit message. It should contain the name of the related bug ticket. (See [A Note About Git Commit Messages for guidelines](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html))
* Review and follow the Eclipse Due Diligence Process
  * [Intellectual Property](https://www.eclipse.org/projects/handbook/#ip)
  * [Due Diligence for Prerequisites](https://www.eclipse.org/projects/handbook/#ip-prereq-diligence)

## Contributing to the NatTable website

To contribute to the website the following repositories need to be cloned:

```
git clone ssh://<user_id>@git.eclipse.org:29418/www.eclipse.org/nattable
git clone ssh://<user_id>@git.eclipse.org:29418/www.eclipse.org/nattable
```

To test locally we recommend a local webserver installation with PHP support, e.g. [XAMPP](https://www.apachefriends.org/de/index.html).

If XAMPP is used, ensure that the two cloned repositories are accessible in the webserver. This can be done for example by setting symbolic links in the *htdocs* directory of XAMPP.

* On Windows open a console as Administrator
* Change directory: *<install_dir>\xampp\htdocs*
* `mklink /D nattable <clone_dir>\nattable`
* `mklink /D "eclipse.org-common" <clone_dir>\eclipse.org-common`

Once the symbolic links are in place, start XAMPP via control center and open *localhost/nattable* in a browser.