package edu.cmu.cc.minisite;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Objects;

/**
 * Task 1:
 * This query simulates the login process of a user
 * and tests whether your backend system is functioning properly.
 * Your web application will receive a pair of UserName and Password,
 * and you need to check your backend database to see if the
 * UserName and Password is a valid pair.
 * You should construct your response accordingly:
 *
 * If YES, send back the userName and Profile Image URL.
 * If NOT, set userName as "Unauthorized" and Profile Image URL as "#".
 */
public class ProfileServlet extends HttpServlet {

    /**
     * JDBC driver of MySQL Connector/J.
     */
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    /**
     * Database name.
     */
    private static final String DB_NAME = "reddit_db";

    /**
     * The endpoint of the database.
     *
     * To avoid hardcoding credentials, use environment variables to include
     * the credentials.
     *
     * e.g., before running "mvn clean package exec:java" to start the server
     * run the following commands to set the environment variables.
     * export MYSQL_HOST=...
     * export MYSQL_NAME=...
     * export MYSQL_PWD=...
     */
    private static String mysqlHost = System.getenv("MYSQL_HOST");
    /**
     * MySQL username.
     */
    private static String mysqlName = System.getenv("MYSQL_NAME");
    /**
     * MySQL Password.
     */
    private static String mysqlPwd = System.getenv("MYSQL_PWD");

    /**
     * The connection (session) with the database.
     * HINT: pay attention to how this is used internally
     */
    private static Connection conn;

    /**
     * MySQL URL.
     */
    private static final String URL = "jdbc:mysql://" + mysqlHost + ":3306/"
            + DB_NAME + "?useSSL=false&serverTimezone=UTC";

    /**
     * Initialize SQL connection. Standard constructor
     *
     * @throws ClassNotFoundException when an application fails to load a class
     * @throws SQLException           on a database access error or other errors
     */
    public ProfileServlet() throws ClassNotFoundException, SQLException {
        conn = getDBConnection();
    }

    /**
     * A special constructor for TDD
     * @param conn  The connection to use
     */
    ProfileServlet(Connection conn) {
        ProfileServlet.conn = conn;
    }

    private Connection getDBConnection() throws SQLException {
        Objects.requireNonNull(mysqlHost);
        Objects.requireNonNull(mysqlName);
        Objects.requireNonNull(mysqlPwd);
        return DriverManager.getConnection(URL, mysqlName, mysqlPwd);
    }

    /**
     * Method that handles HttpServletRequests (GET)
     *
     * @param request  the request object that is passed to the servlet
     * @param response the response object that the servlet
     *                 uses to return the headers to the client
     * @throws IOException      if an input or output error occurs
     */
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("id");
        String pwd = request.getParameter("pwd");
        JsonObject result = validateLoginAndReturnResult(name, pwd);
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(result.toString());
        writer.close();
    }


    /**
     * Method to validate login details and return results
     *
     * @param name  The username supplied via the HttpServletRequest
     * @param pwd   The password supplied via the HttpServletRequest
     * @return A JsonObject with the servlet's response
     */
    JsonObject validateLoginAndReturnResult(String name, String pwd) {
        //Query string to Prepared Statement of the query to perform
        String query = "SELECT username, profile_photo_url FROM users WHERE username = ? AND pwd = ?";
        return executeQuery(query,name,pwd);
    }

    /**
     * Creates a PreparedStatement with the given query and parameters.
     *
     * @param query  the SQL query with placeholders.
     * @param params the parameters to set in the query.
     * @return a PreparedStatement object with parameters set.
     * @throws SQLException if a database access error occurs.
     */
    PreparedStatement getPreparedStmt(String query,String ...params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        for(int i = 0; i < params.length; i++) {
            stmt.setString(i+1, params[i]);// 1 specifies the first parameter in the query
        }
        return stmt;
    }

    /**
     * Executes the query to validates the username and password and retrieves user profile details.
     * @param query  Query to execute
     * @param params List of query params
     * @return A JsonObject with the servlet's response
     */
    public JsonObject executeQuery(String query, String ...params) {
        JsonObject result = new JsonObject();
        try( PreparedStatement stmt = getPreparedStmt(query, params);
            ResultSet rs = stmt.executeQuery() ) {
            if (rs.next()) {
                result = createUserProfileResponse(rs.getString("username"),
                        rs.getString("profile_photo_url"));
            } else {
                result = createUserProfileResponse("Unauthorized", "#");
            }
        } catch (SQLException e) {
            System.err.println("Database error during login validation: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Constructs a JSON object for a valid user profile.
     *
     * @param username the validated username.
     * @param profileUrl the profile image URL for the user.
     * @return a JsonObject containing the user profile information.
     */
    private JsonObject createUserProfileResponse(String username, String profileUrl) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", username);
        jsonObject.addProperty("profile", profileUrl);
        return jsonObject;
    }

    @Override
    public void destroy() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
        super.destroy();
    }
}

