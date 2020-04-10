node {
	def gitBaseAddress = "192.168.1.150:10080"
	def gitBuildAddress = "${gitBaseAddress}/hypercloud/hypercloud-operator.git"
	
	def hcBuildDir = "/var/lib/jenkins/workspace/hypercloud4-operator"
	def imageBuildHome = "/root/HyperCloud-image-build/hypercloud4-operator"

	def version = "${params.majorVersion}.${params.minorVersion}.${params.tinyVersion}.${params.hotfixVersion}"
	def imageTag = "b${version}"
	def binaryHome = "${hcBuildDir}/build"
	def scriptHome = "${hcBuildDir}/scripts"
		
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
		gradleDoBuild()
    }

    stage('file home copy') {
		sh "sudo rm -rf ${imageBuildHome}/hypercloud4-operator/"
		sh "sudo rm -f ${imageBuildHome}/start.sh"
		sh "sudo cp -r ${binaryHome}/hypercloud4-operator ${imageBuildHome}/hypercloud4-operator"
		sh "sudo cp ${binaryHome}/start.sh ${imageBuildHome}/start.sh"
    }
    
	stage('make crd directory') {
		sh "sudo ./${scriptHome}/hypercloud-make-crd-yaml.sh ${version}"
		sh "sudo cp -r ${hcBuildDir}/_yaml_CRD/${version} ${binaryHome}/hypercloud4-operator/_yaml_CRD"
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
}
void gradleDoBuild() {
    sh "./gradlew clean doBuild"
}
