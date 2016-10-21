stage 'build'
def builds = [:]
builds['linux'] = {
    node('linux') {
        git 'https://github.com/wpilibsuite/ntcore.git'
        sh './gradlew clean :native:ntcore:build :native:wpiutil:build ntcoreSourceZip wpiutilSourceZip'
        stash includes: 'native/*/build/libs/**/*.jar, native/*/build/**/*.zip, build/*.zip, build/*.txt', name: 'linux'
    }
}
builds['mac'] = {
    node('mac') {
        git 'https://github.com/wpilibsuite/ntcore.git'
        sh './gradlew clean :native:ntcore:build :native:wpiutil:build ntcoreSourceZip wpiutilSourceZip'
        stash includes: 'native/*/build/libs/**/*.jar, native/*/build/**/*.zip, build/*.zip, build/*.txt', name: 'mac'
    }
}
builds['windows'] = {
    node('windows') {
        git 'https://github.com/wpilibsuite/ntcore.git'
        bat '.\\gradlew.bat clean :native:ntcore:build :native:wpiutil:build ntcoreSourceZip wpiutilSourceZip'
        stash includes: 'native/*/build/libs/**/*.jar, native/*/build/**/*.zip, build/*.zip, build/*.txt', name: 'windows'
    }
}
builds['arm'] = {
    node {
        ws("workspace/${env.JOB_NAME}/arm") {
            git 'https://github.com/wpilibsuite/ntcore.git'
            sh './gradlew clean :arm:wpiutil:build :arm:ntcore:build'
            stash includes: 'arm/*/build/libs/**/*.jar, arm/ntcore/build/ntcore-arm.zip, arm/wpiutil/build/wpiutil-arm.zip', name: 'arm'
        }
    }
}

parallel builds

stage 'combine'
node {
    ws("workspace/${env.JOB_NAME}/combine") {
        git 'https://github.com/333fred/build-tools.git'
        sh 'git clean -xfd'
        dir('uberjar/products') {
            unstash 'linux'
            unstash 'mac'
            unstash 'windows'
            unstash 'arm'
        }
        sh 'chmod +x ./uberjar/gradlew'
        sh 'cd ./uberjar && ./gradlew clean publish'
    }
}

stage 'downstream'
node {
    build job: 'OutlineViewer/OutlineViewer - Development', wait: false
    build job: 'SmartDashboard/SmartDashboard - Development', wait: false
    build job: 'WPILib/WPILib - Development', wait: false
}
