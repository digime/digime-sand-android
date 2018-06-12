def notifier = new me.digi.Slack(this);

pipeline {
    agent {
        node {
            label 'android'
        }
    }

    options {
        skipDefaultCheckout()
    }

    stages {
        stage('checkout') {
            steps {
                deleteDir()

                checkout([
                    $class: 'GitSCM',
                    branches: scm.branches,
                    doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
                    extensions: scm.extensions + [[$class: 'CloneOption', noTags: false, shallow: true, depth: 200, reference: '']],
                    userRemoteConfigs: scm.userRemoteConfigs,
                ])
                //client build logic depends on this command, so we keep it here just as Jenkins config check
                sh "git describe"
            }
        }
        stage('build') {
            steps {
                //build all of modules here
                sh "./gradlew assembleRelease -PBUILD_NUMBER=${env.BUILD_NUMBER}"
            }
        }
        stage('lint') {
            steps {
                sh "./gradlew lint -PBUILD_NUMBER=${env.BUILD_NUMBER}"
            }
        }
        stage('test') {
            steps {
                sh "./gradlew test -PBUILD_NUMBER=${env.BUILD_NUMBER}"
            }
        }
        //stage('android-test') {
        //    steps {
        //        lock(resource: "emulator_${env.NODE_NAME}") {
        //            sh "./gradlew connectedAndroidTest -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        //        }
        //    }
        //}
        stage('archive') {
            when {
                expression {
                    return env.BRANCH_NAME == "master"
                }
            }
            steps {
                dir('app/build/outputs') {
                    //TODO archive only some of builds
                    archiveArtifacts artifacts: '**/*.apk', fingerprint: true;
                    archiveArtifacts artifacts: '**/mapping.txt', fingerprint: true;
                }
            }
        }
    }

    post {
        success {
            script {
                notifier.success()
            }
        }
        always {
            junit '**/TEST-*.xml'

            deleteDir()
        }
    }
}

