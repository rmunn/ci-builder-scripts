/*
 * DSL script for Jenkins Bloom Packaging jobs
 */
//#include utilities/Common.groovy

// Variables
def packagename = 'Bloom'
def distros_tobuild = 'bionic xenial'
def repo = 'git://github.com/BloomBooks/BloomDesktop.git'
def email_recipients = 'stephen_mcconnel@sil.org'
def packagingAgent = 'packager'

def revision = "\$(echo \${GIT_COMMIT} | cut -b 1-6)"
def package_version = '--package-version "\${FULL_BUILD_NUMBER}" '

/*
 * Definition of jobs
 *
 * NOTE: if you want to rename jobs, rename them in Jenkins first, then change the name here
 * and commit and push the changes.
 */

/*
 * We have up to four jobs (for four different branches) for alpha ('master'), betainternal (eg, 'Version4.3'),
 * beta (eg, 'Version4.2'), and release (eg, 'Version4.1').
 * betainternal is used periodically when a new release is almost ready and we are in the process
 * of shifting the previous alpha to beta.  Except for alpha on the master branch, the other jobs all
 * shift branches as new releases are made.
 */
for (version in ['4.6', '4.7', 'master']) {
	switch (version) {
		case '4.6':
			branch = 'Version4.6'
			subdir_name = 'bloom-desktop'
			kind = 'release'
			distros_thisjob = distros_tobuild
			break
		case '4.7':
			branch = 'Version4.7'
			subdir_name = 'bloom-desktop-beta'
			kind = 'beta'
			distros_thisjob = distros_tobuild
			break
		case 'master':
			branch = 'master'
			subdir_name = 'bloom-desktop-alpha'
			kind = 'alpha'
			distros_thisjob = distros_tobuild
			break
	}

	freeStyleJob("Bloom_Packaging-Linux-all-${version}-${kind}") {

		Common.defaultPackagingJob(
			jobContext: delegate,
			packageName: packagename,
			subdirName: subdir_name,
			packageVersion: package_version,
			revision: revision,
			distrosToBuild: distros_thisjob,
			email: email_recipients,
			branch: branch,
			archesToBuild: 'amd64',
			supportedDistros: 'bionic xenial trusty',
			buildMasterBranch: false,
			fullBuildNumber: "0.0.\$BUILD_NUMBER.${revision}",
			nodeLabel: packagingAgent)

		description """
<p>Automatic ("nightly") builds of the Bloom ${branch} branch.</p>
<p>The job is created by the DSL plugin from <i>BloomPackagingJobs.groovy</i> script.</p>
"""

		triggers {
			githubPush()
		}

		Common.gitScm(delegate, repo, "\$BranchOrTagToBuild",
			false, subdir_name, false, true)

		wrappers {
			timeout {
				elastic(300, 3, 90)
				abortBuild()
				writeDescription("Build timed out after {0} minutes")
			}
		}
	}
}
