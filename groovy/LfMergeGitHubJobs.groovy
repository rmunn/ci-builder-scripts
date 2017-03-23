/*
 * Copyright (c) 2016 SIL International
 * This software is licensed under the MIT license (http://opensource.org/licenses/MIT)
 */

/*
 * DSL script for Jenkins LfMerge jobs
 */
import utilities.LfMerge
import utilities.common

// *********************************************************************************************
freeStyleJob('GitHub-LfMerge-Linux-any-master-release') {
	LfMerge.commonLfMergeBuildJob(delegate, '+refs/pull/*:refs/remotes/origin/pr/*', '${sha1}', true, true, true)

	description '''<p>Pre-merge Linux builds of master branch. Triggered by creating a PR on GitHub.<p>
<p>The job is created by the DSL plugin from <i>LfMergeGitHubJobs.groovy</i> script.</p>'''

	common.addGitHubParamAndTrigger(delegate, 'master')
}

// *********************************************************************************************
freeStyleJob('GitHub-Chorus-Linux-any-lfmerge-release') {

	description '''<p>Pre-merge Linux builds of lfmerge branch of Chorus. Triggered by creating a PR on GitHub.<p>
<p>The job is created by the DSL plugin from <i>LfMergeGitHubJobs.groovy</i> script.</p>'''

	common.addGitHubParamAndTrigger(delegate, 'lfmerge')
	LfMerge.generalLfMergeBuildJob(delegate, '+refs/pull/*:refs/remotes/origin/pr/*', '${sha1}', false, false, "sillsdev/chorus", 'linux&&supported')

	wrappers {
		common.addXvfbBuildWrapper(delegate)
	}


	steps {
		shell('''#!/bin/bash
. environ
unset LD_PRELOAD

build/buildupdate.mono.sh

xbuild /t:Test /property:BUILD_NUMBER=0.0.$BUILD_NUMBER.0 /property:Configuration=ReleaseMono /property:excludedCategories=SkipOnBuildServer build/Chorus.proj''')
	}

	publishers {
		configure common.NUnitPublisher('**/TestResults.xml')
	}
}

// *********************************************************************************************
freeStyleJob('GitHub-Chorus-Win32-lfmerge-release') {

	description '''<p>Pre-merge Windows builds of lfmerge branch of Chorus. Triggered by creating a PR on GitHub.<p>
<p>The job is created by the DSL plugin from <i>LfMergeGitHubJobs.groovy</i> script.</p>'''

	common.addGitHubParamAndTrigger(delegate, 'lfmerge', 'windows')
	LfMerge.generalLfMergeBuildJob(delegate, '+refs/pull/*:refs/remotes/origin/pr/*', '${sha1}', false, false, "sillsdev/chorus", 'windows&&timeInSync')

	steps {
		common.addGetDependenciesWindowsBuildStep(delegate, 'build/buildupdate.win.sh')

		common.addMsBuildStep(delegate, 'build\\Chorus.proj', '/t:Test /property:BUILD_NUMBER=0.0.%BUILD_NUMBER%.0 /property:Configuration=Release')
	}

	publishers {
		configure common.NUnitPublisher('**/TestResults.xml')
	}
}

// *********************************************************************************************
freeStyleJob('GitHub-FlexBridge-Linux-any-lfmerge-release') {

	description '''<p>Pre-merge Linux builds of lfmerge branch of FLExBridge. Triggered by creating a PR on GitHub.<p>
<p>The job is created by the DSL plugin from <i>LfMergeGitHubJobs.groovy</i> script.</p>'''

	common.addGitHubParamAndTrigger(delegate, 'lfmerge')
	LfMerge.generalLfMergeBuildJob(delegate, '+refs/pull/*:refs/remotes/origin/pr/*', '${sha1}', false, false, "sillsdev/flexbridge", 'linux&&supported')

	wrappers {
		common.addXvfbBuildWrapper(delegate)
	}

	steps {
		shell('''#!/bin/bash
BUILD=ReleaseMono
. environ

./download_dependencies_linux.sh

xbuild /t:Test /property:BUILD_NUMBER=0.0.$BUILD_NUMBER.0 /property:BUILD_VCS_NUMBER=$GIT_COMMIT /property:Configuration=$BUILD build/FLExBridge.proj''')
	}

	publishers {
		configure common.NUnitPublisher('**/TestResults.xml')
	}
}

// *********************************************************************************************
freeStyleJob('GitHub-FlexBridge-Win32-lfmerge-release') {

	description '''<p>Pre-merge Windows builds of lfmerge branch of FLExBridge. Triggered by creating a PR on GitHub.<p>
<p>The job is created by the DSL plugin from <i>LfMergeGitHubJobs.groovy</i> script.</p>'''

	common.addGitHubParamAndTrigger(delegate, 'lfmerge', 'windows')
	LfMerge.generalLfMergeBuildJob(delegate, '+refs/pull/*:refs/remotes/origin/pr/*', '${sha1}', false, false, "sillsdev/flexbridge", 'windows&&timeInSync')

	steps {
		common.addGetDependenciesWindowsBuildStep(delegate, './download_dependencies_windows.sh')

		common.addMsBuildStep(delegate, 'build\\FLExBridge.proj', '/t:Test /property:BUILD_NUMBER=0.0.%BUILD_NUMBER%.0 /property:BUILD_VCS_NUMBER=%GIT_COMMIT% /property:Configuration=Release')
	}

	publishers {
		configure common.NUnitPublisher('**/TestResults.xml')
	}
}
