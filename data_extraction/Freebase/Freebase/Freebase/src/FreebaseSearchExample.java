
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FreebaseSearchExample {
  public static String API_KEY = "AIzaSyCfD4cpo8NgVu5Izt32eBOZlp9e_oRQ2oI";
  public static String artist="Shakira";
  public static String album= "Rabiosa";
  public static void main(String[] args) {
	    try {
	      HttpTransport httpTransport = new NetHttpTransport();
	      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	      JSONParser parser = new JSONParser();
	      String query=" [{\"type\":\"/music/artist\",\"name\":\""+ artist +"\",\"origin\":[],\"album\":{\"name\":\""+album+"\",\"genre\":[],\"release_date\":[]},\"/common/topic/social_media_presence\":[{}]}]";
	      GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
	      url.put("key", FreebaseSearchExample.API_KEY);
	      url.put("query", query);
	      HttpRequest request = requestFactory.buildGetRequest(url);
	      HttpResponse httpResponse = request.execute();
	      JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
	      JSONArray results = (JSONArray)response.get("result");
	      for (Object result : results) {
	        System.out.println(JsonPath.read(result,"$.origin").toString());
	      }
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
  }
}