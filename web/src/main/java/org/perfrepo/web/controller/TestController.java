/**
 *
 * PerfRepo
 *
 * Copyright (C) 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.perfrepo.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.perfrepo.model.Metric;
import org.perfrepo.model.MetricComparator;
import org.perfrepo.model.Test;
import org.perfrepo.model.to.TestExecutionSearchTO;
import org.perfrepo.model.user.User;
import org.perfrepo.web.service.TestService;
import org.perfrepo.web.service.UserService;
import org.perfrepo.web.service.exceptions.ServiceException;
import org.perfrepo.web.session.SearchCriteriaSession;
import org.perfrepo.web.util.MessageUtils;
import org.perfrepo.web.viewscope.ViewScoped;

/**
 * Backing bean for editing and displaying details of {@link Test}.
 *
 * @author Michal Linhard (mlinhard@redhat.com)
 */
@Named
@ViewScoped
public class TestController extends BaseController {

	private static final long serialVersionUID = 370202307562230671L;
	private static final Logger log = Logger.getLogger(TestController.class);

	private boolean editMode;
	private boolean createMode;
	private Long testId;

	@Inject
	private TestService testService;

	@Inject
	private UserService userService;

	@Inject
	private SearchCriteriaSession criteriaSession;

	private Test test = null;
	private MetricDetails metricDetails = new MetricDetails();

	/**
	 * called on preRenderView
	 */
	public void preRender() throws Exception {
		reloadSessionMessages();
		if (testId == null) {
			if (!createMode) {
				log.error("No test ID supplied");
				redirectWithMessage("/", ERROR, "page.test.errorNoTestId");
			} else {
				if (test == null) {
					test = new Test();
				}
			}
		} else {
			if (test == null) {
				test = testService.getFullTest(testId);
				if (test == null) {
					log.error("Can't find test with id " + testId);
					redirectWithMessage("/", ERROR, "page.test.errorTestNotFound", testId);
				} else {
					metricDetails.selectionAssignedMetrics = testService.getAvailableMetrics(test);
				}
			}
		}
	}

	public void listTestExecutions() {
		//clear criterias
		TestExecutionSearchTO criteriaSession = this.criteriaSession.getExecutionSearchCriteria();
		criteriaSession.setStartedFrom(null);
		criteriaSession.setStartedTo(null);
		criteriaSession.setTags(null);
		criteriaSession.setTestName(null);

		criteriaSession.setTestUID(test.getUid());
		redirect("/exec/search");
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	public String update() {
		if (test == null) {
			throw new IllegalStateException("test is null");
		}
		testService.updateTest(test);
		redirectWithMessage("/test/" + testId, INFO, "page.test.updatedSuccessfully");
		return null;
	}

	public List<Metric> getMetricsList() {
		List<Metric> mlist = new ArrayList<Metric>();
		if (test != null) {
			mlist.addAll(test.getMetrics());
		}
		Collections.sort(mlist);
		return mlist;
	}

	public String create() {
		if (test == null) {
			throw new IllegalStateException("test is null");
		}
		try {
			Test createdTest = testService.createTest(test);
			redirectWithMessage("/test/" + createdTest.getId(), INFO, "page.test.createdSuccesfully", createdTest.getId());
		} catch (ServiceException e) {
			addMessage(e);
		} catch (SecurityException e) {
			addMessage(ERROR, "page.test.errorSecurityException", e.getMessage());
		} catch (EJBException e) {
			if (e.getCause() != null && e.getCause().getClass() == SecurityException.class) {
				addMessage(ERROR, "page.test.errorSecurityException", e.getCause().getMessage());
			} else {
				throw e;
			}
		}
		return null;
	}

	public Long getTestId() {
		return testId;
	}

	public void setTestId(Long testId) {
		this.testId = testId;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public boolean isCreateMode() {
		return createMode;
	}

	public void setCreateMode(boolean createMode) {
		this.createMode = createMode;
	}

	public MetricDetails getMetricDetails() {
		return metricDetails;
	}

	public List<String> getUserGroups() {
		return userService.getLoggedUserGroupNames();
	}

   public void addSubscriber() {
      User currentUser = userService.getLoggedUser();
      testService.addSubscriber(currentUser, test);
   }

   public void removeSubscriber() {
      User currentUser = userService.getLoggedUser();
      testService.removeSubscriber(currentUser, test);
   }

   public boolean isSubscribed() {
      User currentUser = userService.getLoggedUser();
      return testService.isUserSubscribed(currentUser, test);
   }

	public class MetricDetails {
		private boolean createMode;
		private Metric metric;
		private Long selectedAssignedMetricId;
		private List<Metric> selectionAssignedMetrics;

		public Metric getMetric() {
			return metric;
		}

		public void setMetricForUpdate(Metric metric) {
			this.metric = metric;
		}

		public void setEmptyMetric() {
			this.metric = new Metric();
		}

		public void unsetMetric() {
			this.metric = null;
		}

		public boolean isCreateMode() {
			return createMode;
		}

		public void setCreateMode(boolean createMode) {
			this.createMode = createMode;
		}

		public List<Metric> getSelectionAssignedMetrics() {
			return selectionAssignedMetrics;
		}

		public boolean isSelectionMetricVisible() {
			return selectionAssignedMetrics != null && !selectionAssignedMetrics.isEmpty();
		}

		public Long getSelectedAssignedMetricId() {
			return selectedAssignedMetricId;
		}

		public void setSelectedAssignedMetricId(Long selectedAssignedMetricId) {
			this.selectedAssignedMetricId = selectedAssignedMetricId;
		}

		public void addAssignedMetric() {
			if (selectedAssignedMetricId == null || selectionAssignedMetrics == null) {
				redirectWithMessage("/test/" + testId, ERROR, "page.test.errorNoAssignedMetric");
			} else {
				Metric selectedAssignedMetric = null;
				for (Metric m : selectionAssignedMetrics) {
					if (selectedAssignedMetricId.equals(m.getId())) {
						selectedAssignedMetric = m;
						break;
					}
				}
				if (selectedAssignedMetric == null) {
					redirectWithMessage("/test/" + testId, ERROR, "page.test.errorNoAssignedMetric");
					return;
				}
				try {
					testService.addMetric(test, selectedAssignedMetric);
					redirectWithMessage("/test/" + testId, INFO, "page.test.metricSuccessfullyAssigned", selectedAssignedMetric.getName());
				} catch (ServiceException e) {
					addSessionMessage(e);
					redirect("/test/" + testId);
				}
			}
		}

		public MetricComparator[] getMetricComparators() {
			return MetricComparator.values();
		}

		public String getEnumLabel(MetricComparator mc) {
			return MessageUtils.getEnum(mc);
		}

		public void createMetric() {
			try {
				testService.addMetric(test, metric);
				redirectWithMessage("/test/" + testId, INFO, "page.test.metricSuccessfullyCreated", metric.getName());
			} catch (ServiceException e) {
				addSessionMessage(e);
				redirect("/test/" + testId);
			}
		}

		public void updateMetric() {
			try {
				testService.updateMetric(test, metric);
			} catch (ServiceException e) {
				addSessionMessage(e);
				redirect("/test/" + testId);
			}
		}

		public void deleteMetric(Metric metricToDelete) {
			try {
				testService.removeMetric(test, metricToDelete);
				redirectWithMessage("/test/" + testId, INFO, "page.test.metricSuccessfullyDeleted", metricToDelete.getName());
			} catch (ServiceException e) {
				addSessionMessage(e);
				redirect("/test/" + testId);
			}
		}
	}
}