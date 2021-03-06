package data_extraction.facebook;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import common.DBHelper;
import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Page;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;

public class FacebookPosts extends Thread {

    private final static String SINCE_YEAR = "2010";
    private final static int LIMIT = 50;

    public FacebookPosts() {
        super();
    }

    public void run() {

        System.out.println("Gathering facebook posts...");

        Facebook facebook = new FacebookFactory().getInstance();

        DBHelper db = DBHelper.getInstance();

        DBCursor cursor = db.findAllArtists();

        try {

            while (cursor.hasNext()) {
                DBObject item = cursor.next();

                try {
                    System.out.println("Facebook: treating " + item.get("name"));
                    BasicDBList facebook_id = (BasicDBList) item.get("facebook_id");

                    //To avoid useless sleeps
                    Boolean parsingFB = false;
                    if (facebook_id != null) {

                        for (Object id : facebook_id) {
                            parsingFB = true;

                            String str_id = (String) id;

                            Page result = facebook.getPage(str_id);

                            /* if page not found (or other shitty facebook API behaviour like
                             * considering a person as a page), continue with next one
                             */
                            if (result == null || result.getLikes() == null || result.getTalkingAboutCount() == null) {
                                continue;
                            }

                            db.updateArtistLikes(item, result.getLikes(), result.getTalkingAboutCount());

                            int offset = 0;
                            int count = 0;
                            boolean hasData = true;

                            while (hasData) {
                                ResponseList<Post> feed = facebook.getFeed(result.getId(), new Reading().since(SINCE_YEAR).limit(LIMIT).offset(offset));
                                for (Post post : feed) {
                                    BasicDBList commentsList = new BasicDBList();
                                    for (Comment comment : post.getComments()) {
                                        BasicDBObject comment1 = new BasicDBObject("message", comment.getMessage());
                                        comment1.append("likes", comment.getLikeCount()).append("date", comment.getCreatedTime());
                                        commentsList.add(comment1);
                                    }
                                    // TODO:
                                    // A facebook cover change is included in a post, we should maybe discard them
                                    db.insertFbPosts((ObjectId) item.get("_id"), post.getCreatedTime(), post.getMessage(), post.getLikes().size(),
                                            (post.getSharesCount() == null) ? 0 : post.getSharesCount(), post.getPicture() == null || post.getPicture().equals(""),
                                            commentsList);
                                }
                                offset += LIMIT;
                                count += feed.size();
                                hasData = (feed.size() == LIMIT);
                            }

                            System.out.println("Found " + count + " facebook posts for " + item.get("name"));
                        }
                    } else {
                        System.out.println("Discarding " + item.get("name") + ", no facebook id");
                    }

                    // Sleeping 1s to avoid being blocked by facebook
                    try {
                        if (parsingFB) {
                            sleep(1000);
                        }
                    } catch (InterruptedException e) {

                    }
                } catch (FacebookException e) {
                    // something went wrong, just discard this entry
                    System.err.println("Something went wrong with facebook API " + e.getMessage());
                }
            }
        } finally {
            cursor.close();
        }

        System.out.println("Facebook gathering finished.");
    }
}
