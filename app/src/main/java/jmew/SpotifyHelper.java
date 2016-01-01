package jmew;

/**
 * Created by Mew on 2015-11-15.
 */
public class SpotifyHelper {
    private static String userId;
    private static String authToken;

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String authToken) {
        SpotifyHelper.authToken = authToken;
    }


    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        SpotifyHelper.userId = userId;
    }
}
