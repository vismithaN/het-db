package edu.cmu.cc.minisite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bson.conversions.Bson;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.mongodb.client.model.Filters.in;


/**
 * In this task you will populate a user's timeline.
 * This task helps you understand the concept of fan-out. 
 * Practice writing complex fan-out queries that span multiple databases.
 *
 * Task 4 (1):
 * Get the name and profile of the user as you did in Task 1
 * Put them as fields in the result JSON object
 *
 * Task 4 (2);
 * Get the follower name and profiles as you did in Task 2
 * Put them in the result JSON object as one array
 *
 * Task 4 (3):
 * From the user's followees, get the 30 most popular comments
 * and put them in the result JSON object as one JSON array.
 * (Remember to find their parent and grandparent)
 *
 * The posts should be sorted:
 * First by ups in descending order.
 * Break tie by the timestamp in descending order.
 */
public class TimelineServlet extends HttpServlet {

    private final FollowerServlet followerServlet;
    private final ProfileServlet profileServlet;
    private final HomepageServlet homepageServlet;
    /**
     * Your initialization code goes here.
     */
    public TimelineServlet() throws SQLException, ClassNotFoundException {
        this.followerServlet = new FollowerServlet();
        this.profileServlet = new ProfileServlet();
        this.homepageServlet = new HomepageServlet();
    }

    /**
     * Don't modify this method.
     *
     * @param request  the request object that is passed to the servlet
     * @param response the response object that the servlet
     *                 uses to return the headers to the client
     * @throws IOException      if an input or output error occurs
     * @throws ServletException if the request for the HEAD
     *                          could not be handled
     */
    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {

        // DON'T modify this method.
        String id = request.getParameter("id");
        String result = getTimeline(id);
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print(result);
        writer.close();
    }

    /**
     * Builds and returns the timeline for the given user ID.
     * @param id User ID.
     * @return JSON string of the user's timeline.
     */
    public String getTimeline(String id) {
        JsonObject result = new JsonObject();
        try{
            addUserProfile(result, id);    // Task (1)
            addFollowers(result, id);      // Task (2)
            addFolloweesComments(result, id); // Task (3)
        } catch (Exception e) {
            System.err.println("Error: Unable to retrieve timeline for user: " + id + e.getMessage());
        }
        return result.toString();
    }

    /**
     * Adds the user's profile information (name and profile picture) to the result.
     * @param result JsonObject to populate.
     * @param id     User ID.
     */
    private void addUserProfile(JsonObject result, String id) throws Exception {
        String query = "SELECT username, profile_photo_url FROM users WHERE username = ?";
        try {
            JsonObject userProfile = profileServlet.executeQuery(query,id);
            result.addProperty("name", userProfile.get("name").getAsString());
            result.addProperty("profile", userProfile.get("profile").getAsString());
        } catch (Exception e) {
            System.err.println("Error retrieving profile for user ID: " + id + ". " + e.getMessage());
            throw e;
        }
    }

    /**
     * Adds the user's followers to the result JSON object.
     * @param result JsonObject to populate.
     * @param id     User ID.
     */
    private void addFollowers(JsonObject result, String id) {
        JsonArray followers = followerServlet.getFollowers(id);
        result.add("followers", followers);
    }


    /**
     * Retrieves and returns a list of followee IDs for a given user.
     * @param id User ID.
     * @return List of followee usernames.
     */
    private List<String> getFollowees(String id) {
        List<String> followees = new ArrayList<>();
        String query = "MATCH (follower:User)-[:FOLLOWS]->(followee:User) " +
                "WHERE follower.username = $id " +
                "RETURN followee.username AS username";
        try {
            StatementResult rs = followerServlet.executeQuery(query,id);
            while (rs.hasNext()) {
                Record record = rs.next();
                followees.add(record.get("username").asString());
            }
        } catch (Exception e) {
            System.err.println("Error: Unable to retrieve followees for user ID: " + id + e.getMessage());
        }
        return followees;
    }

    /**
     * Adds the top 30 comments from the user's followees to the result JSON object.
     * Each comment includes potential parent and grandparent relationships.
     * @param result JsonObject to populate.
     * @param id     User ID.
     */
    private void addFolloweesComments(JsonObject result, String id) {
        List<String> followeesIDList = getFollowees(id);
        Bson filter = in("uid",followeesIDList);

        JsonArray followeesComments = homepageServlet.executeQuery(filter);
        int size = followeesComments.size();

        JsonArray topFolloweesComments = new JsonArray();
        for(int i=0; i<Math.min(size,30); i++){
            JsonObject comment = followeesComments.get(i).getAsJsonObject();
            addCommentHierarchy(comment);
            topFolloweesComments.add(comment);
        }
       result.add("comments", topFolloweesComments);
    }

    /**
     * Adds parent and grandparent information to a comment if available.
     * @param comment Comment JSON object.
     */
    private void addCommentHierarchy(JsonObject comment) {
        String parentId = comment.has("parent_id") ? comment.get("parent_id").getAsString() : null;
        if (parentId != null) {
            JsonObject parentComment = homepageServlet.findParentsComments(parentId);
            if (parentComment != null) {
                comment.add("parent", parentComment);
                String grandParentId = parentComment.has("parent_id") ? parentComment.get("parent_id").getAsString() : null;
                if (grandParentId != null) {
                    JsonObject grandParentComment = homepageServlet.findParentsComments(grandParentId);
                    if (grandParentComment != null) {
                        comment.add("grand_parent", grandParentComment);
                    }
                }
            }
        }
    }


}

