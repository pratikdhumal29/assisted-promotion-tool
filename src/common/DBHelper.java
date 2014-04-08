package common;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

/**
 *
 * @author mikaelcastellani, arthurgiroux
 */

public class DBHelper {
    
    private static volatile DBHelper instance = null;
    
    //Collections
    private static final String DBNAME = "team15";
    
    private static final String ARTISTSCOLLECTION = "artistsCollection";
    private static final String CLEANARTISTSCOLLECTION = "cleanArtistsCollection";
    private static final String ALBUMSCOLLECTION = "albumsCollection";
    private static final String CLEANALBUMSCOLLECTION = "cleanAlbumsCollection";
    private static final String FBPOSTSCOLLECTION = "fbpostsCollection";
    private static final String TWEETSCOLLECTION = "tweetsCollection";
    private static final String CLEANFBPOSTSCOLLECTION = "cleanFbpostsCollection";
    private static final String CLEANTWEETSCOLLECTION = "cleanTweetsCollection";
    
    private DB db;
    private DBCollection albumsCollection;
    private DBCollection artistsCollection;
    private DBCollection fbpostsCollection;
    private DBCollection tweetsCollection;
    private DBCollection cleanAlbumsCollection;
    private DBCollection cleanArtistsCollection;
    private DBCollection cleanFbpostsCollection;
    private DBCollection cleanTweetsCollection;
    
    
    public final static DBHelper getInstance() {
        
        // See wikipedia article for information on this shenanigan
        if (DBHelper.instance == null) {
            synchronized(DBHelper.class) {
                if (DBHelper.instance == null) {
                    DBHelper.instance = new DBHelper(Settings.getInstance().getProperty("mongodb_host"), 27017);
                }
            }
        }
        return DBHelper.instance;
    }
    
    public DB getDb() {
        return db;
    }
    
    
    private DBHelper(String url, int port) {
        MongoClient mongoClient;
        try {
            mongoClient = new MongoClient(url, port);
            DB db = mongoClient.getDB(DBNAME);
            db.authenticate(Settings.getInstance().getProperty("mongodb_user"),
                            Settings.getInstance().getProperty("mongodb_password").toCharArray());
            
            artistsCollection = db.getCollection(ARTISTSCOLLECTION);
            albumsCollection = db.getCollection(ALBUMSCOLLECTION);
            cleanArtistsCollection = db.getCollection(CLEANARTISTSCOLLECTION);
            cleanAlbumsCollection = db.getCollection(CLEANALBUMSCOLLECTION);
            fbpostsCollection = db.getCollection(FBPOSTSCOLLECTION);
            tweetsCollection = db.getCollection(TWEETSCOLLECTION);
            
        } catch (UnknownHostException e) {
            System.out.println("Something went wrong while connecting to the database");
            System.exit(1);
        }
    }
    
    public ObjectId insertArtist(String name, String country, double hotness, double familiary, List<String> facebook_id,
                                 List<String> twitter_id) {
        BasicDBObject newArtist = new BasicDBObject("name", name).
        append("country", country).
        append("hotness", hotness).
        append("familiarity", familiary).
        append("facebook_id", facebook_id).
        append("twitter_id", twitter_id);
        artistsCollection.insert(newArtist);
        
        // return the id of the insertion
        return (ObjectId) newArtist.get("_id");
    }
    
    public void updateArtistLikes(DBObject artist, int likes, int talking_about) {
        artist.put("facebook_likes", likes);
        artist.put("facebook_talking_about", talking_about);
        cleanArtistsCollection.save(artist);
    }
    
    public void updateArtistFollowers(DBObject artist, int followers){
        artist.put("twitter_followers", followers);
        cleanArtistsCollection.save(artist);
    }
    
    public void insertAlbum(ObjectId artist_id, String name, Date release_date, List<String> genre) {
        BasicDBObject newAlbum = new BasicDBObject("name", name).
        append("artist_id", artist_id).
        append("release_date", release_date).
        append("genre", genre);
        albumsCollection.insert(newAlbum);
        
    }
    
    public void insertFbPosts(ObjectId artist_id, Date date, String message, int likes, int shares,
                              boolean picture_attached) {
        BasicDBObject new_post = new BasicDBObject("artist_id", artist_id).
        append("date", date).
        append("message", message).
        append("likes", likes).
        append("shares", shares).
        append("picture_attached", picture_attached);
        fbpostsCollection.insert(new_post);
    }
    
    public void insertTweet(ObjectId artist_id, Date date, String source, String message, int retweets,
                            List<String> hashtags) {
        BasicDBObject new_post = new BasicDBObject("artist_id", artist_id).
        append("date", date).
        append("source", source).
        append("message", message).
        append("retweets", retweets).
        append("hashtags", hashtags);
        tweetsCollection.insert(new_post);
    }
    
    public void emptyAll() {
        artistsCollection.remove(new BasicDBObject());
        albumsCollection.remove(new BasicDBObject());
        fbpostsCollection.remove(new BasicDBObject());
        tweetsCollection.remove(new BasicDBObject());
    }
    
    public void emptyFacebookAndTwitterCollections() {
        fbpostsCollection.remove(new BasicDBObject());
        tweetsCollection.remove(new BasicDBObject());
    }
    
    public DBCursor findAllArtists() {
        return cleanArtistsCollection.find();
    }
    
    public DBCursor findAllAlbums(){
        return albumsCollection.find();
    }
    
    public long countArtists() {
        return cleanArtistsCollection.count();
    }
    
    public long countAlbums() {
        return cleanAlbumsCollection.count();
    }
    
    public long countFacebookPosts() {
        return fbpostsCollection.count();
    }
    
    public long countTweets() {
        return tweetsCollection.count();
    }
    
    public boolean hasTweetsParsed(ObjectId artist_id){
        BasicDBObject post = new BasicDBObject("artist_id", artist_id);
        DBObject obj = tweetsCollection.findOne(post);
        return obj!=null;
    }
    
    public boolean artistExists(ObjectId artist_id) {
        BasicDBObject post = new BasicDBObject("_id", artist_id);
        DBObject obj = cleanArtistsCollection.findOne(post);
        return obj!=null;
    }
    
    public void insertCleanAlbum(DBObject album){
        cleanAlbumsCollection.insert(album);
    }
    
    public void insertCleanTweet(DBObject tweet){
        cleanTweetsCollection.insert(tweet);
    }
    
    public void insertCleanFbpost(DBObject fbpost){
        cleanFbpostsCollection.insert(fbpost);
    }
    
}