node {
	def gitBaseAddress = "192.168.1.150:10080"
	def gitBuildAddress = "${gitBaseAddress}/hypercloud/hypercloud-operator.git"
	
	def hcBuildDir = "/var/lib/jenkins/workspace/hypercloud-3.1"
	def imageBuildHome = "/root/HyperCloud-image-build/application-patch-image"

	def version = "${params.majorVersion}.${params.minorVersion}.${params.tinyVersion}.${params.hotfixVersion}"
	def imageTag = "b${version}"
	def globalVersion = "HyperCloud-server:b${version}"
	def poHome = "${hcBuildDir}/build/base_po_home"
	def watcherHome = "${hcBuildDir}/build/k8swatcher"
			
	def userName = "seonho_choi"
	def userEmail = "seonho_choi@tmax.co.kr"

    stage('gradle build') {
        //deleteDir()
		new File("${hcBuildDir}").mkdir()
		dir ("${hcBuildDir}") {
			git branch: "${params.buildBranch}",
			credentialsId: '${userName}',
			url: "http://${gitBuildAddress}"
		}
		gradleMakeHome("${hcBuildDir}", "${version}")
    }

    stage('file home copy') {
		sh "sudo rm -rf ${imageBuildHome}/base_po_home/"
		sh "sudo rm -rf ${imageBuildHome}/k8swatcher/"
		sh "sudo cp -r ${poHome} ${imageBuildHome}/base_po_home"
		sh "sudo cp -r ${watcherHome} ${imageBuildHome}/k8swatcher"
    }
    
	stage('sql-merge') {
		sh "sudo sh ${imageBuildHome}/sql_scripts/integrated_update.sh ${version}"
		sh "sudo sh ${imageBuildHome}/sql_scripts/integrated_rollback.sh ${version}"
	}    
    
	stage('image build & push'){
		sh "sudo docker build --tag 192.168.6.110:5000/hyper-cloud-server:${imageTag} ${imageBuildHome}/"
		sh "sudo docker push 192.168.6.110:5000/hyper-cloud-server:${imageTag}"
	}
	
	stage('sql-integrated'){
		dir ("${hcBuildDir}") {
			sh "git checkout ${params.buildBranch}"

			sh "git config --global user.name ${userName}"
			sh "git config --global user.email ${userEmail}"
			sh "git config --global credential.helper store"

			sh "git fetch --all"
			sh "git reset --hard origin/${params.buildBranch}"
			sh "git pull origin ${params.buildBranch}"

			sh "sudo cp ${imageBuildHome}/base_po_home/sql/prozone/integrated/integrated_create.sql ${hcBuildDir}/_base-po-home/sql/prozone/integrated/integrated_create.sql"
			sh "sudo cp ${imageBuildHome}/base_po_home/sql/prozone/integrated/integrated_update.sql ${hcBuildDir}/_base-po-home/sql/prozone/integrated/integrated_update.sql"
			sh "sudo cp ${imageBuildHome}/base_po_home/sql/prozone/integrated/integrated_rollback.sql ${hcBuildDir}/_base-po-home/sql/prozone/integrated/integrated_rollback.sql"
			sh "sudo cp ${imageBuildHome}/base_po_home/sql/prozone/temp/temp_rollback.sql ${hcBuildDir}/_base-po-home/sql/prozone/temp/temp_rollback.sql"
			sh "sudo cp ${imageBuildHome}/base_po_home/sql/prozone/temp/temp_update.sql ${hcBuildDir}/_base-po-home/sql/prozone/temp/temp_update.sql"

			sh "git add ."

			sh (script:'git commit -m "[SQL-Integrated] Hyper Cloud - ${version} Integrated SQL" || true')


			sh "sudo git push -u origin +${params.buildBranch}"

			sh "git fetch --all"
			sh "git reset --hard origin/${params.buildBranch}"
			sh "git pull origin ${params.buildBranch}"
		}	
	}	
    
	stage('send file to FTP'){
		dir ("${hcBuildDir}") {
			sh "sudo tar -cvf ${imageBuildHome}/po_tar/hyper-cloud-${version}.tar ${imageBuildHome}/base_po_home/ ${imageBuildHome}/k8swatcher/"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${imageBuildHome}/po_tar/hyper-cloud-${version}.tar ck-ftp@192.168.1.150:/home/ck-ftp/binary/hyper-cloud-server"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${hcBuildDir}/_api_swagger/broker_api.yaml ck-ftp@192.168.1.150:/home/ck-ftp/api/3.1/hypercloud-broker"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${hcBuildDir}/_api_swagger/provider_api.yaml ck-ftp@192.168.1.150:/home/ck-ftp/api/3.1/hypercloud-provider"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${hcBuildDir}/_api_swagger/consumer_api.yaml ck-ftp@192.168.1.150:/home/ck-ftp/api/3.1/hypercloud-consumer"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${imageBuildHome}/base_po_home/sql/prozone/integrated/integrated_create.sql ck-ftp@192.168.1.150:/home/ck-ftp/sql/3.1/hyper-cloud"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${imageBuildHome}/base_po_home/sql/prozone/integrated/integrated_update.sql ck-ftp@192.168.1.150:/home/ck-ftp/sql/3.1/hyper-cloud"
			sh "sudo sshpass -p 'ck-ftp' scp -o StrictHostKeyChecking=no ${imageBuildHome}/base_po_home/sql/prozone/integrated/integrated_rollback.sql ck-ftp@192.168.1.150:/home/ck-ftp/sql/3.1/hyper-cloud"
		}	
	}    
}
void gradleMakeHome(dirPath,version) {
    sh "./gradlew clean makeHome -PbuildVersion=${version}"
}
