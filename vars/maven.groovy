def call(body)
{
    def mavenGoals = config.mavenGoals
    def branchName = config.branchName
    def dockerBuildImageName = config.dockerImageName
    def pomLocationName = config.pomLocation
    def maven_opts = config.mavenOpts ?: '"'+'-Xms256 -Xmx1024m -Xss1024k'+'"'
    def GET_BUILD_USER = 'NONE'
    wrap([$class: 'BuildUser']) {
        GET_BUILD_USER = sh ( script: 'echo "${BUILD_USER}"', returnStdout: true).trim()
    }
    echo "${mavenGoals}"
    docker.image("${dockerBuildImageName}"){
        sh """
        export MAVEN_OPTS=${maven_opts}
        mvn ${mavenGoals} -f ${WORKSPACE}/${pomLocationName} -Dmaven.test.skip=true
        """
    }
}
