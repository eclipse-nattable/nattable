# How To: Release NatTable

This document describes the process on how to release Nebula NatTable.

## Preparation

 * Ensure that everything is checked in to the master branch
 * Create and push a release branch for the version that should be released (e.g. releases/2.0.2)
 * Create a release in the project management: https://projects.eclipse.org/node/1463/create-release
   * Ensure that the project information is updated (e.g. set the correct Release Type)
 * Add the new version to the Bugzilla Manager

## Release to Eclipse

 * Trigger the Jenkins Job to release to Eclipse: https://ci.eclipse.org/nattable/job/nattable-release-eclipse/
 * Build with parameters
   * Select the branch that should be build and released
   * Specify the version tag that should be used, which is needed for the folder in the download area

## Release to Maven Central

 * Update the version to remove -SNAPSHOT and .qualifier from any version numbers in the release branch via `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=VERSION`
 * Trigger the Jenkins Job to release to Eclipse: https://ci.eclipse.org/nattable/job/nattable-release-maven/
 * Build with parameters
   * Select the branch that should be build and released

 * Once the build finishes successfully, you have deployed the artifacts to the OSSRH staging area. According to the documentation you now need to release the deployment to Maven Central.

   * Login to https://oss.sonatype.org/
   * In the left menu check for *Build Promotion - Staging Repositories*
   * Select your repository and validate if it meets your expectations
   * If everything is correct *Close* the repository, otherwise *Drop* it and fix any issues
   * Once it is closed, press the *Release* button to trigger the release to the Central repository

 * Create a tag for the new version based on the last commit
 * Finally update the version in the master branch via `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=NEW_VERSION_NUMBER.qualifier`

## Update the website

 * Update download.php to add the new version for downloading
 * Update documentation.php to correct the link to the API docs
 * Update pages/_index.html to add the news about the new release
 * Update documentation/start.html to correct the link to the API docs
 * Update documentation/examples_application.html to correct the links to the examples applications


 More details on the publishing process of Eclipse projects to Maven Central can be found [here](https://wiki.eclipse.org/Tycho:How_to_deploy_to_a_Maven_repository)