/*
 * Copyright (c) 2017 SIL International
 * This software is licensed under the MIT license (http://opensource.org/licenses/MIT)
 */
/*
 * DSL script for WeSay Jenkins views
 */

//#include utilities/CommonViews.groovy

/* Definition of views */

nestedView('WeSay') {
/*	configure { view ->
		view / defaultView('All')
	}*/
	views {
		CommonViews.addPackageBuildsListView(delegate, 'WeSay', '^WeSay.*_Packaging-.*')
	}
}
