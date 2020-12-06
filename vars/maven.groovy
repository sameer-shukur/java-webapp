def call(body)
{
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
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
    docker.image("${dockerBuildImageName}").inside('-v /app/maven:/app/maven --net=host'){
        sh """
        export MAVEN_OPTS=${maven_opts}
        export JAVA_OPTS=${java_opts}
        mvn -s ${WORKSPACE}/settings.xml ${mavenGoals} -f ${WORKSPACE}/${pomLocationName}
        """
    }
}