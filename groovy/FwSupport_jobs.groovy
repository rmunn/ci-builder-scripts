/*
 * DSL script to create FwSupport job
 */

/*
 * Definition of jobs
 */

freeStyleJob('FwSupportTools-Linux-packager-debug-checkoutonly') {
	description '''<p>This job keeps the <i>FwSupportTools</i> directory up-to-date on the packaging machine.</p>
<p>The job is created by the DSL plugin from <i>FwSupport-jobs.groovy</i> script.<p>'''

	parameters {
		stringParam("GERRIT_REFSPEC", 'refs/heads/develop', "The git branch")
	}

	label 'packager'

	customWorkspace("\$HOME/FwSupportTools")

	scm {
		git {
			remote {
				url("git://gerrit.lsdev.sil.org/FwSupportTools.git")
				refspec("\$GERRIT_REFSPEC")
			}
			branch("develop")
			extensions {
				choosingStrategy {
					gerritTrigger()
				}
			}
		}
	}

	triggers {
		gerrit {
			events {
				refUpdated()
			}
			project('FwSupportTools', 'develop')
		}
	}
}

