package edu.cmu.cc.minisite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;


/**
 * Task 3:
 * Implement your logic to return all the comments authored by this user.
 *
 * You should sort the comments by ups in descending order (from the largest to the smallest one).
 * If there is a tie in the ups, sort the comments in descending order by their timestamp.
 */
public class HomepageServlet extends HttpServlet {

    /**
     * The endpoint of the database.
     *
     * To avoid hardcoding credentials, use environment variables to include
     * the credentials.
     *
     * e.g., before running "mvn clean package exec:java" to start the server
     * run the following commands to set the environment variables.
     * export MONGO_HOST=...
     */
    private static final String MONGO_HOST = System.getenv("MONGO_HOST");
    /**
     * MongoDB server URL.
     */
    private static final String URL = "mongodb://" + MONGO_HOST + ":27017";
    /**
     * Database name.
     */
    private static final String DB_NAME = "reddit_db";
    /**
     * Collection name.
     */
    private static final String COLLECTION_NAME = "posts";
    /**
     * MongoDB connection.
     */
    private static MongoCollection<Document> collection;

    private MongoClient mongoClient;
    /**
     * Initialize the connection.
     */
    public HomepageServlet() {
        Objects.requireNonNull(MONGO_HOST);
        MongoClientURI connectionString = new MongoClientURI(URL);
        mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        collection = database.getCollection(COLLECTION_NAME);
    }

    /**
     * Implement this method.
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

        JsonObject result = new JsonObject();
        String id = request.getParameter("id");
        result.add("comments", getComments(id));
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(result.toString());
        writer.close();
    }

    /**
     * Retrieves and returns all comments authored by the specified user.
     * @param username The user ID of the comment author.
     * @return JsonArray containing user comments in JSON format.
     */
    public JsonArray getComments(String username) {
        //Populate filter for username
        Bson filter = eq("uid",username);
        return executeQuery(username,filter);
    }

    /**
     * Executes a MongoDB query to fetch comments.
     * Sorts by descending "ups" and then by descending "timestamp" in case of ties.
     * Excludes MongoDB's internal "_id" field
     * @param username The user ID for which to retrieve comments.
     * @param filter   The filter to apply to the MongoDB query.
     * @return JsonArray of comments sorted by popularity and timestamp.
     */
    public JsonArray executeQuery(String username,  Bson filter) {
        JsonArray comments = new JsonArray();
        Bson orderBySort = Sorts.orderBy(Sorts.descending("ups"), Sorts.descending("timestamp"));
        Bson projection = exclude("_id");

        try (MongoCursor<Document> cursor = collection.find(filter)
                .sort(orderBySort).projection(projection).iterator()) {
            while (cursor.hasNext()) {
                JsonObject jsonObject = new JsonParser().parse(cursor.next().toJson()).getAsJsonObject();
                comments.add(jsonObject);
            }
        } catch (Exception e) {
            System.err.println("Error: Unable to retrieve comments for user: " + username + e.getMessage());
        }
        return comments;
    }

    /**
     * Closes the MongoDB connection when the servlet is destroyed.
     */
    @Override
    public void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        super.destroy();
    }
}

