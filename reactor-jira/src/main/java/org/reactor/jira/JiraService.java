package org.reactor.jira;

import static com.google.common.collect.Lists.newArrayList;
import org.reactor.ReactorProcessingException;
import org.reactor.jira.response.JiraIssue;
import org.reactor.jira.response.JiraSprint;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.RapidView;
import net.rcarz.jiraclient.greenhopper.Sprint;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

public class JiraService {

    private static final int RAPID_VIEW_FIRST = 1;
    private static final String STATUS_UNKNOWN = "Unknown";
    private GreenHopperClient greenHopperClient;
    private JiraClient jiraClient;
    private String projectName;

    public static JiraService forServerDetails(String serverUrl, String username, String password, String projectName)
            throws URISyntaxException {
        return new JiraService(serverUrl, username, password, projectName);
    }

    private JiraService() {}

    public JiraService(String serverUrl, String username, String password, String projectName)
            throws URISyntaxException {
        this.projectName = projectName;
        BasicCredentials creds = new BasicCredentials(username, password);
        jiraClient = new JiraClient(serverUrl, creds);
        greenHopperClient = new GreenHopperClient(jiraClient);
    }

    public List<JiraIssue> getAllIssues() {
        List<JiraIssue> result = newArrayList();
        SearchResult searchResult = null;
        try {
            searchResult = jiraClient.searchIssues("project=" + projectName);
        } catch (JiraException e) {
            throw new ReactorProcessingException(e);
        }

        for (Issue issue : searchResult.issues) {
            JiraIssue jiraIssue = new JiraIssue(issue.getKey(), issue.getSummary(), issue.getDescription(), issue
                .getStatus() != null ? issue.getStatus().getName() : STATUS_UNKNOWN);
            result.add(jiraIssue);
        }
        return result;
    }

    public List<JiraSprint> getAllSprints() {
        List<JiraSprint> result = newArrayList();
        try {
            RapidView board = greenHopperClient.getRapidView(RAPID_VIEW_FIRST);
            List<Sprint> sprints = board.getSprints();
            for (Sprint sprint : sprints) {
                Date startDate = sprint.getStartDate() != null ? sprint.getStartDate().toDate() : null;
                Date completeDate = sprint.getCompleteDate() != null ? sprint.getCompleteDate().toDate() : null;
                JiraSprint jiraSprint = new JiraSprint(sprint.getName(), startDate, completeDate);
                result.add(jiraSprint);
            }
        } catch (JiraException e) {
            throw new ReactorProcessingException(e);
        }
        return result;
    }

}
