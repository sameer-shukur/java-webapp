def call(body)
{
      def dockerImageName= 'sameershukur/java-webapp:$BUILD_NUMBER'
      stage('SCM Checkout'){
         git 'https://github.com/sameer-shukur/java-webapp'
      }
      stage('Build'){
         // Get maven home path and build
         def mvnHome =  tool name: 'Maven 3.5.4', type: 'maven'   
         sh "${mvnHome}/bin/mvn package -Dmaven.test.skip=true"
      }       
     
     stage ('Test'){
         def mvnHome =  tool name: 'Maven 3.5.4', type: 'maven'    
         sh "${mvnHome}/bin/mvn verify; sleep 3"
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
      