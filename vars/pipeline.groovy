def call(body)
{
	  def gitURL = config.gitURL
	  def repoBranch = config.repoBranch
      def dockerImageName = 'sameershukur/java-webapp:$BUILD_NUMBER'
	  
      stage('SCM Checkout'){
         gitClone "${gitURL}","${repoBranch}"
      }
      stage('Build'){
         docker.image('sameershukur/maven-3.6.3:v1')
		 {
			sh """
			export MAVEN_OPTS="-Xms256m -Xmx1024m -Xss1024k"
			mvn ${mavenGoals} -f ${WORKSPACE}/pom.xml -Dmaven.test.skip=true
			"""
		 }
      }       
     
     stage ('Test'){
          docker.image('sameershukur/maven-3.6.3:v1')  
		  {
			sh """
			export MAVEN_OPTS="-Xms256m -Xmx1024m -Xss1024k"
			mvn verify
			sleep 3
			"""
		  }
	}
      
     stage('Build Docker Image'){         
           sh "docker build -t ${dockerImageName} ."
      }  
   
      stage('Publish Docker Image'){
         withCredentials([string(credentialsId: 'dockerpwd', variable: 'dockerPWD')]) {
              sh "docker login -u sameershukur -p ${dockerPWD}"
         }
        sh "docker push ${dockerImageName}"
      }
/*      
    stage('Run Docker Image'){
            def dockerContainerName = 'javadockerapp_$BUILD_NUMBER'
            def changingPermission='sudo chmod +x stopscript.sh'
            def scriptRunner='sudo ./stopscript.sh'           
            def dockerRun= "sudo docker run -p 8082:8080 -d --name ${dockerContainerName} ${dockerImageName}" 
            withCredentials([string(credentialsId: 'deploymentserverpwd', variable: 'dpPWD')]) {
                  sh "sshpass -p ${dpPWD} ssh -o StrictHostKeyChecking=no sameer@52.76.172.196" 
                  sh "sshpass -p ${dpPWD} scp -r stopscript.sh sameer@52.76.172.196:/home/sameer" 
                  sh "sshpass -p ${dpPWD} ssh -o StrictHostKeyChecking=no sameer@52.76.172.196 ${changingPermission}"
                  sh "sshpass -p ${dpPWD} ssh -o StrictHostKeyChecking=no sameer@52.76.172.196 ${scriptRunner}"
                  sh "sshpass -p ${dpPWD} ssh -o StrictHostKeyChecking=no sameer@52.76.172.196 ${dockerRun}"
            }
 */ 
      }  
         
  }
