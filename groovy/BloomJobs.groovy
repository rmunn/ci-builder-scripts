/*
 * DSL script for Jenkins Bloom jobs
 */
import utilities.common;
import utilities.Bloom;

/*
 * Definition of jobs
 *
 * NOTE: if you want to rename jobs, rename them in Jenkins first, then change the name here
 * and commit and push the changes.
 */

job {
	name 'Bloom-Wrapper-Trigger-debug';

	description '''
<p>Wrapper job for Bloom builds. This job kicks off several other builds after a new
change got merged and collects the results.</p>
<p>The job is created by the DSL plugin from <i>BloomJobs.groovy</i> script.</p>
''';

	priority(100);

	label 'linux'

	scm {
		git {
			remote {
				github("BloomBooks/BloomDesktop");
			}
			branch('*/master')
		}
	}

	triggers {
		githubPush()
	}

	steps {
		// Trigger downstream build
		common.addTriggerDownstreamBuildStep(delegate,
			'Bloom-Win32-default-debug,Bloom-Linux-any-default-debug,Bloom-Linux-any-master--JSTests')

		common.addTriggerDownstreamBuildStep(delegate,
			'Bloom-Linux-any-default-debug-Tests, Bloom-Win32-default-debug-Tests')

	}

	common.buildPublishers(delegate, 365, 100);
}

// *********************************************************************************************
job {
	Bloom.defaultBuildJob(delegate, 'Bloom-Linux-any-default-debug',
		'Linux builds of master branch');

	label 'ubuntu && supported';

	steps {
		// Install certificates
		common.addInstallPackagesBuildStep(delegate);

		// Get dependencies
		common.addGetDependenciesBuildStep(delegate);

		// Build
		common.addXbuildBuildStep(delegate, 'Bloom\\ VS2010.sln');

		// Trigger downstream build
		common.addTriggerDownstreamBuildStep(delegate, 'Bloom-Linux-any-default-debug-Tests')
	}
}

// *********************************************************************************************
job {
	Bloom.defaultBuildJob(delegate, 'Bloom-Win32-default-debug',
		'Windows builds of master branch');

	label 'windows';

	steps {
		// Get dependencies
		common.addGetDependenciesWindowsBuildStep(delegate)

		// Trigger test run
		common.addTriggerDownstreamBuildStep(delegate, 'Bloom-Win32-default-debug-Tests',
			'ARTIFACTS_TAG=$BUILD_TAG')
	}

	configure common.MsBuildBuilder('Bloom VS2010.sln')
}


// *********************************************************************************************
job {
	Bloom.defaultBuildJob(delegate, 'Bloom-Linux-any-default-debug-Tests',
		'Run unit tests.');

	label 'linux';

	customWorkspace '/home/jenkins/workspace/Bloom-Linux-any-default-debug';

	configure common.XvfbBuildWrapper();
	configure common.RunOnSameNodeAs('Bloom-Linux-any-default-debug', true);

	steps {
		// Run unit tests
		common.addRunUnitTestsLinuxBuildStep(delegate, 'BloomTests.dll')
	}

	publishers {
		configure common.NUnitPublisher('output/Debug/BloomTests.dll.results.xml')
	}
}

// *********************************************************************************************
job {
	Bloom.defaultBuildJob(delegate, 'Bloom-Win32-default-debug-Tests',
		'Run Bloom unit tests.');

	parameters {
		stringParam("ARTIFACTS_TAG", "", "The artifact tag");
		stringParam("UPSTREAM_BUILD_TAG", "", "The upstream build tag.");
	}

	label 'windows';

	configure common.RunOnSameNodeAs('Bloom-Win32-default-debug', true);

	steps {
		// Run unit tests
		common.addRunUnitTestsWindowsBuildStep(delegate, 'BloomTests.dll')

		// this is needed so that upstream aggregation of unit tests works
		common.addMagicAggregationFileWindows(delegate);
	}

	publishers {
		fingerprint('magic.txt')
		archiveArtifacts('output/Debug/BloomTests.dll.results.xml')
		configure common.NUnitPublisher('output/Debug/BloomTests.dll.results.xml')
	}
}

// *********************************************************************************************
job {
	name 'Bloom-Linux-any-master--JSTests';

	description '''
<p>This job runs JS unit tests for Bloom.</p>
<p>The job is created by the DSL plugin from <i>BloomJobs.groovy</i> script.</p>
''';

	label 'linux && !wheezy';

	scm {
		git {
			remote {
				github("BloomBooks/BloomDesktop", "git");
				refspec('+refs/pull/*:refs/remotes/origin/pr/*')
			}
			branch('master')
		}
	}

	wrappers {
		timestamps()
		timeout {
			noActivity 180
		}
	}

	// Job DSL currently doesn't support to abort the build in the case of a timeout.
	// Therefore we have to use this clumsy way to add it.
	configure { project ->
		project / 'buildWrappers' / 'hudson.plugins.build__timeout.BuildTimeoutWrapper' / 'operationList' {
			'hudson.plugins.build__timeout.operations.AbortOperation'()
		}
	}

	steps {
		// Install nodejs dependencies
		common.addInstallKarmaBuildStep(delegate);

		// Get dependencies
		common.addGetDependenciesBuildStep(delegate);

		// run unit tests
		common.addRunJsTestsBuildStep(delegate, 'src/BloomBrowserUI');
	}

	publishers {
		archiveJunit('src/BloomBrowserUI/test-results.xml');
	}

	common.buildPublishers(delegate, 365, 100);
}
