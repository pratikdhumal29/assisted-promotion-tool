/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import com.mongodb.BasicDBList;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static common.DBHelper.*;

/**
 *
 * @author mikaelcastellani
 */
public class Recommender {
    
    public static float thresholdCosim = 0.9f;
    
    public static float FBLikesWeight = 0.4f;
    public static float TwitterFollowersWeight = 0.4f;
    public static float NumberOfAlbumsWeight = 0.0f;
    public static float RegionWeight = 0.2f;
    public static float CategoryWeight = 0.0f;
    
    

    public static void main(String[] args) {
        try {
            if(FBLikesWeight+TwitterFollowersWeight+NumberOfAlbumsWeight+RegionWeight+CategoryWeight>1){
                System.out.println("Check the weights");
                System.exit(0);
            }
            
            
            DBHelper db = DBHelper.getInstance();

            DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

            Set<DBObject> similarResults = new HashSet<DBObject>();

            int counter = 0;
            DBObject obj1 = cursor.next();
            while (cursor.hasNext()) {
                DBObject obj2 = cursor.next();

                Double cosim = computeSimilarity(obj1, obj2);
                //System.out.println("Cosim calculated" + cosim);
                if (cosim > thresholdCosim) {
                    counter++;
                    similarResults.add(obj2);
                }

            }
            System.out.println(counter);
            

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

    }

    public static double computeSimilarity(DBObject obj1, DBObject obj2) {

        int sameRegion = obj1.get(REGION) == obj2.get(REGION) ? 1 : 0;

        int sameCategories = 0;
        BasicDBList categoryList1 = (BasicDBList) obj1.get(CATEGORY);
        BasicDBList categoryList2 = (BasicDBList) obj2.get(CATEGORY);
        if (categoryList1 != null && categoryList2 != null) {
            for (Object category : categoryList1) {
                for (Object category2 : categoryList2) {
                    if (((String) category).equals(((String) category2))) {
                        sameCategories = 1;
                        break;
                    }
                }
            }
        }
        double cossimFb;
        double cossimTw;
        double cossimAlbums;

        if ((Integer) (obj1.get(FACEBOOKLIKES)) == (Integer) (obj2.get(FACEBOOKLIKES))) {
            cossimFb = 1;
        } else {
            cossimFb = (double) Math.min((Integer) (obj1.get(FACEBOOKLIKES)), (Integer) (obj2.get(FACEBOOKLIKES)))
                    / Math.max((Integer) (obj1.get(FACEBOOKLIKES)), (Integer) (obj2.get(FACEBOOKLIKES)));
        }

        if ((Integer) (obj1.get(TWITTERFOLLOWERS)) == (Integer) (obj2.get(TWITTERFOLLOWERS))) {
            cossimTw = 1;
        } else {
            cossimTw = (double) Math.min((Integer) (obj1.get(TWITTERFOLLOWERS)), (Integer) (obj2.get(TWITTERFOLLOWERS)))
                    / Math.max((Integer) (obj1.get(TWITTERFOLLOWERS)), (Integer) (obj2.get(TWITTERFOLLOWERS)));
        }

        if ((Integer) (obj1.get(NUMBEROFALBUMS)) == (Integer) (obj2.get(NUMBEROFALBUMS))) {
            cossimAlbums = 1;
        } else {
            cossimAlbums = (double) Math.min((Integer) (obj1.get(NUMBEROFALBUMS)), (Integer) (obj2.get(NUMBEROFALBUMS)))
                    / Math.max((Integer) (obj1.get(NUMBEROFALBUMS)), (Integer) (obj2.get(NUMBEROFALBUMS)));
        }

//        a_norm = Math.sqrt(cossimFb + cossimTw + cossimAlbums + sameRegion + sameCategories);
//        //b_norm = Math.sqrt(cossimFb + cossimTw + cossimAlbums + sameRegion + sameCategories);
//
//        sum = 
//        
//        dot_prod = (A.nb_albums * B.nb_albums) + (A.country * B.country) + (A.genre * B.genre) + (A.fb_likes * B.fb_likes) + (A.tw_fol * B.tw_fol);
//        cos_sim = dot_prod / (a_norm * b_norm);
        return (FBLikesWeight*cossimFb + 
                TwitterFollowersWeight*cossimTw + 
                NumberOfAlbumsWeight*cossimAlbums +
                RegionWeight*sameRegion + 
                CategoryWeight*sameCategories);
    }
}
