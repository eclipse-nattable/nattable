pipeline {
    agent {
        kubernetes {
            label 'migration'
        }
    }
    tools {
        maven 'apache-maven-latest'
        jdk 'openjdk-jdk17-latest'
    }
    environment {
		BUILD_DIR = "${WORKSPACE}/org.eclipse.nebula.widgets.nattable.examples.e4.product/target/products/"
		DMG = org.eclipse.nebula.widgets.nattable.examples.e4.product-macosx.cocoa.x86_64.dmg
        PRIMARY_BUNDLE_ID = "app-bundle"
		SNAPSHOTS_DIR = /home/data/httpd/download.eclipse.org/nattable/snapshots
		SNAPSHOT_BUILD_DIR = "${SNAPSHOTS_DIR}/${BUILD_NUMBER}"
		SSH_HOST="genie.nattable@projects-storage.eclipse.org"
    }
    stages {
        stage('Build') {
            steps {
                wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
                  sh 'mvn clean compile javadoc:aggregate verify -Psign -B'
                }
            }
        }
        stage('Notarize') {
            steps {
                sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                    sh """
                       echo "Notarize..."
					   pushd $BUILD_DIR
                       RESPONSE=\$(curl -s -X POST -F file=@${DMG} -F 'options={"primaryBundleId": "'${PRIMARY_BUNDLE_ID}'", "staple": true};type=application/json' https://cbi.eclipse.org/macos/xcrun/notarize)
      
					   UUID=\$(echo $RESPONSE | grep -Po '"uuid"\\s*:\\s*"\\K[^"]+')
					   STATUS=\$(echo $RESPONSE | grep -Po '"status"\\s*:\\s*"\\K[^"]+')
							
					   while [[ ${STATUS} == 'IN_PROGRESS' ]]; do
						 sleep 1m
						 RESPONSE=\$(curl -s https://cbi.eclipse.org/macos/xcrun/${UUID}/status)
					     STATUS=\$(echo $RESPONSE | grep -Po '"status"\\s*:\\s*"\\K[^"]+')
					   done
							
					   if [[ ${STATUS} != 'COMPLETE' ]]; then
						 echo "Notarization failed: ${RESPONSE}"
						 exit 1
					   fi
							
					   rm "${DMG}"
							
					   curl -JO https://cbi.eclipse.org/macos/xcrun/${UUID}/download
					   popd
                       """
                }
            }
        }
        stage('Deploy') {
            steps {
                sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                    sh """
                       echo "Deploying..."
                       # Copy from Jenkins to download.eclipse.org
					   ssh ${SSH_HOST} mkdir -p ${SNAPSHOT_BUILD_DIR}/repository
					   scp -r ${WORKSPACE}/org.eclipse.nebula.widgets.nattable.updatesite/target/repository/* ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}/repository
					   scp ${WORKSPACE}/org.eclipse.nebula.widgets.nattable.examples/target/*.jar ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}
					   scp ${WORKSPACE}/org.eclipse.nebula.widgets.nattable.examples.e4.product/target/products/*.zip ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}
					   scp ${WORKSPACE}/org.eclipse.nebula.widgets.nattable.examples.e4.product/target/products/*.tar.gz ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}
					   scp ${WORKSPACE}/org.eclipse.nebula.widgets.nattable.examples.e4.product/target/products/*.dmg ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}
					   scp -r ${WORKSPACE}/target/site/apidocs ${SSH_HOST}:${SNAPSHOT_BUILD_DIR}

					   # ssh on download.eclipse.org
					   ssh ${SSH_HOST} << EOF

					   # create the .metadata file
					   echo ${BUILD_ID} > ${SNAPSHOT_BUILD_DIR}/.metadata

					   # Zip
					   cd ${SNAPSHOT_BUILD_DIR}/repository
					   zip -r ../repository.zip *

					   # Delete old latest snapshot
					   rm -rf ${SNAPSHOTS_DIR}/latest

					   # Delete old snapshot directories; only keep latest 5
					   ls -dt ${SNAPSHOTS_DIR}/*/ | tail -n +7 | xargs rm -rf

					   # Copy latest snapshot build to latest snapshot repository
					   cp -r ${SNAPSHOT_BUILD_DIR} ${SNAPSHOTS_DIR}/latest

					   # Copy latest snapshot build to release (only enabled for release build) 
					   # cp -r ${SNAPSHOT_BUILD_DIR} /home/data/httpd/download.eclipse.org/nattable/releases/2.0.0
					   EOF
                       """
                }
            }
        }
	}
    post {
        // send a mail on unsuccessful and fixed builds
        unsuccessful { // means unstable || failure || aborted
            emailext subject: 'Build $BUILD_STATUS $PROJECT_NAME #$BUILD_NUMBER!', 
            body: '''Check console output at $BUILD_URL to view the results.''',
            recipientProviders: [culprits(), requestor()], 
            to: 'nattable-dev@eclipse.org'
        }
        fixed { // back to normal
            emailext subject: 'Build $BUILD_STATUS $PROJECT_NAME #$BUILD_NUMBER!', 
            body: '''Check console output at $BUILD_URL to view the results.''',
            recipientProviders: [culprits(), requestor()], 
            to: 'nattable-dev@eclipse.org'
        }
    }
}