import java.text.SimpleDateFormat
import hudson.model.Actionable
def call(body)
{
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  def gitURL = config.gitURL
  def repoBranch = config.repoBranch
  def dockerImageName = 'sameershukur/java-webapp:$BUILD_NUMBER'
  def failed_stage = 'NONE'
  def current_stage = env.STAGE_NAME
  try
  {
   	node('master')
	  {  
		currentBuild.result = 'SUCCESS'
		print(manager.getResult())
		stage('SCM Checkout')
		{
		checkout scm
	//	echo "${current_stage}"
		} //End of checkout stage
  
		stage('Build')
		  {
			docker.image('https://hub.docker.com/repository/docker/sameershukur/maven-3.6.3').inside
			{
			sh """
			export MAVEN_OPTS="-Xms256m -Xmx1024m -Xss1024k"
			mn ${mavenGoals} -f ${WORKSPACE}/pom.xml -Dmaven.test.skip=true
			"""
			}
		//	echo "${current_stage} is SUCCESS!"
		  }   //End of build stage    
     
     		stage ('Test')
		  {
          		docker.image('https://hub.docker.com/repository/docker/sameershukur/maven-3.6.3').inside
			{
			sh """
			export MAVEN_OPTS="-Xms256m -Xmx1024m -Xss1024k"
			mvn verify
			sleep 3
			"""
		  	}
		//	echo "${current_stage} is SUCCESS!"
		  } //End of test stage
      
		stage('Build Docker Image')
		  {         
			sh "docker build -t ${dockerImageName} ."
		//  	echo "${current_stage} is SUCCESS!"
		  }  //End of Build docker image
   
		stage('Publish Docker Image')
		  {
			withCredentials([string(credentialsId: 'dockerpwd', variable: 'dockerPWD')]) {
			sh "docker login -u sameershukur -p ${dockerPWD}" 
			}
			sh "docker push ${dockerImageName}" 
		//	echo "${current_stage} is SUCCESS!"
		  } //End of publish docker image
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
		      }  
		*/         
	  } //End of node block
  } //End of try node
catch(err)
	{
		currentBuild.result = 'FAILURE'
		//Mail on failure
		mail bcc: '', body:"${err}", cc: '', from: '', replyTo: '', subject: 'Job failed', to: 'sameer.shukur.m@gmail.com'
	}
} //End of body
