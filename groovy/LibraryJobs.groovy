/*
 * Copyright (c) 2016-2019 SIL International
 * This software is licensed under the MIT license (http://opensource.org/licenses/MIT)
 */

for (repo in ['icu-dotnet', 'icu4c', 'SIL.BuildTasks']) {
	multibranchPipelineJob(repo) {
		description """<p>Builds of ${repo}</p>
	<p>The job is created by the DSL plugin from <i>LibraryJobs.groovy</i> script.</p>"""

		branchSources {
			github {
				id(repo)
				repoOwner('sillsdev')
				repository(repo)
				scanCredentialsId('github-sillsdevgerrit')
				if (repo != 'icu4c') {
					includes('master PR-*')
				}
				buildOriginBranch(true)
				buildOriginBranchWithPR(false)
				buildOriginPRMerge(true)
				buildForkPRMerge(true)
			}

			orphanedItemStrategy {
				discardOldItems {
					daysToKeep(60)
					numToKeep(10)
				}
			}

			triggers {
				// check once a day if not otherwise run
				periodicFolderTrigger {
					interval('1d')
				}
			}
		}

		if (repo == 'icu4c') {
			factory {
				workflowBranchProjectFactory {
					scriptPath('icu4c/Jenkinsfile')
				}
			}
		}
	}
}
