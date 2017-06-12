#!groovy

hipchatSend message: 'Starting development build...', room: 'ActivityInfo DEV'

node('ai-builder') {

    jdk = tool name: 'JDK 1.8'
    env.JAVA_HOME = "${jdk}"

    stage('Build') {
        git branch: 'development',
            credentialsId: '7f2e3024-fbc5-4603-aca7-e14141eb75d7',
            url: 'git@github.com:bedatadriven/activityinfo.git'

        try {
            sh './gradlew test server:build'
        } catch(e) {
            hipchatSend message: 'Build Failed', room: 'ActivityInfo DEV'
            throw e;
        }
   }
   stage('Deploy') {
        appengine sdkName: 'AppEngine SDK',
            action: 'update',
            path: 'server/build/exploded-app/',
            applicationId: 'ai-dev',
            version: 'qa'

        hipchatSend(
            message: "Development build deployed to https://dev-dot-ai-dev-appspot.com/",
            room: 'ActivityInfo DEV')
   }
   stage('Results') {
      junit '**/TEST-*.xml'
   }
}
