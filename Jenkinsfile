node {
	def gitBaseAddress = "192.168.1.150:10080"
	def gitBuildAddress = "${gitBaseAddress}/hypercloud/hypercloud-operator.git"
	
	def hcBuildDir = "/var/lib/jenkins/workspace/hypercloud4-operator"
	def imageBuildHome = "/root/HyperCloud-image-build/hypercloud4-operator"

	def version = "${params.majorVersion}.${params.minorVersion}.${params.tinyVersion}.${params.hotfixVersion}"
	def preVersion = "${params.majorVersion}.${params.minorVersion}.${params.tinyVersion}.${params.preHotfixVersion}"
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
		gradleDoBuild("${hcBuildDir}")
    }

    stage('file home copy') {
		sh "sudo rm -rf ${imageBuildHome}/hypercloud4-operator/"
		sh "sudo rm -f ${imageBuildHome}/start.sh"
		sh "sudo cp -r ${binaryHome}/hypercloud4-operator ${imageBuildHome}/hypercloud4-operator"
		sh "sudo cp ${binaryHome}/start.sh ${imageBuildHome}/start.sh"
    }
    
	stage('make crd directory') {
		sh "sudo sh ${scriptHome}/hypercloud-make-crd-yaml.sh ${version}"
		sh "sudo cp -r ${hcBuildDir}/_yaml_CRD/${version} ${imageBuildHome}/hypercloud4-operator/_yaml_CRD"
	}
    
	stage('make change log'){
		sh "sudo sh ${scriptHome}/hypercloud-make-changelog.sh ${version} ${preVersion}"
	}
	
	stage('build & push image'){
		sh "sudo docker build --tag 192.168.6.110:5000/hypercloud4-operator:${imageTag} ${imageBuildHome}/"
		sh "sudo docker push 192.168.6.110:5000/hypercloud4-operator:${imageTag}"
	}
	
	stage('git commit & push'){
		dir ("${hcBuildDir}") {
			sh "git checkout ${params.buildBranch}"

			sh "git config --global user.name ${userName}"
			sh "git config --global user.email ${userEmail}"
			sh "git config --global credential.helper store"

			sh "git fetch --all"
			sh "git reset --hard origin/${params.buildBranch}"
			sh "git pull origin ${params.buildBranch}"

			sh "git add -A"

			sh (script:'git commit -m "[Version-Up] make changelog & make new crd directory" || true')

			sh "sudo git push -u origin +${params.buildBranch}"

			sh "git fetch --all"
			sh "git reset --hard origin/${params.buildBranch}"
			sh "git pull origin ${params.buildBranch}"
			
			//sh "git tag v${version}"
			//sh "sudo git push origin v${version}"
		}	
	}	
}

void gradleDoBuild(dirPath) {
    sh "./gradlew clean doBuild"
}