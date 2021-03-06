/*
 * Copyright (c) 2017 SIL International
 * This software is licensed under the MIT license (http://opensource.org/licenses/MIT)
 */

/*
 * some Common definitions for Bloom related jobs
 */

class Bloom {
	static void generalBloomBuildJob(jobContext, useTimeout = true) {
		jobContext.with {
			logRotator(365, 100)

			wrappers {
				colorizeOutput()
				timestamps()
				if (useTimeout) {
					timeout {
						noActivity 360
						abortBuild()
					}
				}
			}

			Common.buildPublishers(delegate, 365, 100)
		}
	}

	static void defaultBuildJob(jobContext, descriptionVal, useTimeout = true) {
		generalBloomBuildJob(jobContext, useTimeout)

		jobContext.with {
			description """<p>$descriptionVal. This job gets triggered by Bloom-Wrapper-Trigger-release.<p>
<p>The job is created by the DSL plugin from <i>BloomJobs.groovy</i> script.</p>"""

			scm {
				git {
					remote {
						github("BloomBooks/BloomDesktop", "git")
					}
					branch('*/master')
				}
			}

		}
	}

	static void defaultGitHubPRBuildJob(jobContext, descriptionVal, useTimeout = true) {
		generalBloomBuildJob(jobContext, useTimeout)
		jobContext.with {
			description """<p>$descriptionVal. This job gets triggered by GitHub-Bloom-Wrapper-*.<p>
<p>The job is created by the DSL plugin from <i>BloomGitHubJobs.groovy</i> script.</p>"""

			parameters {
				stringParam("sha1", "",
					"What pull request to build, e.g. origin/pr/9/head")
			}

			scm {
				git {
					remote {
						github("BloomBooks/BloomDesktop", "git")
						refspec('+refs/pull/*:refs/remotes/origin/pr/*')
					}
					branch('${sha1}')
				}
			}
		}
	}

	static void addInstallKarmaBuildStep(stepContext) {
		stepContext.with {
			// NOTE: we create `node` as a symlink. Debian has renamed it to `nodejs` but karma etc
			// expects it as `node`.
			shell('''#!/bin/bash -e
PATH="$HOME/bin:$PATH"
if ! which node > /dev/null; then
	mkdir -p ~/bin
	if [ -f /usr/bin/nodejs ]; then
		ln -s /usr/bin/nodejs ~/bin/node
	fi
fi
cd 'src/BloomBrowserUI'
npm install
# junit is required for running the tests on Jenkins
npm install junit karma-junit-reporter
npm run build
''')
		}
	}

	static void addRunJsTestsBuildStep(stepContext) {
		stepContext.with {
			// Run unit tests
			shell('''#!/bin/bash -e
echo "Running unit tests"
cd 'src/BloomBrowserUI'
PATH="$HOME/bin:$PATH"
NODE_PATH=/usr/lib/nodejs:/usr/lib/node_modules:/usr/share/javascript
export NODE_PATH
node_modules/.bin/karma start --reporters junit --single-run --browsers Firefox --capture-timeout 15000 || true
''')
		}
	}

}

