package edu.cmu.cc.minisite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bson.conversions.Bson;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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

    /**
     * Your initialization code goes here.
     */
    public TimelineServlet() {

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
     * Method to get given user's timeline.
     *
     * @param id user id
     * @return timeline of this user
     */
    private String getTimeline(String id) {
        JsonObject result = new JsonObject();
        // TODO: implement this method
        try{
            task1(result,id); // Task1
            task2(result,id); // Task2
            task3(result,id); // Task3
        } catch (Exception e) {
            System.err.println("Error: Unable to retrieve timeline for user: " + id +e.getMessage());
        }
        return result.toString();
    }

    /**
     * Task 4 (2);
     * Get the follower name and profiles as you did in Task 2
     * Put them in the result JSON object as one array
     * @param result JsonObject to populate
     * @param id userID
     */
    private void task2(JsonObject result, String id) {
        FollowerServlet followerServlet = new FollowerServlet();
        JsonArray followers = followerServlet.getFollowers(id);
        result.add("followers", followers);
    }

    /**
     * Get Followers List of a user
     * @param id
     * @return
     */
    private JsonArray getFollowees(String id) {
        JsonArray followees = new JsonArray();
        FollowerServlet followerServlet = new FollowerServlet();
        String query = "MATCH (follower:User)-[:FOLLOWS]->(followee:User) " +
                "WHERE follower.username = $id " +
                "RETURN followee.username AS username";
        try {
            StatementResult rs = followerServlet.getStatementResult(query,id);
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
     *  Task 4 (3):
     *  From the user's followees, get the 30 most popular comments
     *  and put them in the result JSON object as one JSON array.
     *  (Remember to find their parent and grandparent)
     * @param result
     * @param id
     */
    private void task3(JsonObject result, String id) {
        HomepageServlet homepageServlet = new HomepageServlet();
        JsonArray followeesIDList = getFollowees(id);
        Bson filter = in("uid",followeesIDList);
        JsonArray followeesComments = homepageServlet.executeQuery(id,filter);
        int size = followeesComments.size();

        JsonArray topfolloweesComments = new JsonArray();
        for(int i=0; i<Math.min(size,30); i++){
            JsonElement child = followeesComments.get(i);
            JsonObject parent = homepageServlet.findParentsComments(child.getAsJsonObject().get("cid").getAsString());
            if(parent!=null) {
                followeesComments.get(i).getAsJsonObject().add("parent", parent);
                JsonObject grandParent = homepageServlet.findParentsComments(parent.get("cid").getAsString());
                if(grandParent!=null) {
                    followeesComments.get(i).getAsJsonObject().add("grand_parent", grandParent);
                }
            }
            topfolloweesComments.add(followeesComments.get(i));
        }
       result.add("comments", topfolloweesComments);
    }

    /**
     * Get the name and profile of the user as you did in Task 1
     * Put them as fields in the result JSON object
     */
    private void task1(JsonObject result, String username) throws SQLException, ClassNotFoundException {
        ProfileServlet profileServlet = new ProfileServlet();
        String query = "SELECT username, profile_photo_url FROM users WHERE username = ?";
        JsonObject userProfile = profileServlet.execute(query,username);
        result.addProperty("name", userProfile.get("name").getAsString());
        result.addProperty("profile", userProfile.get("profile").getAsString());
    }
}

