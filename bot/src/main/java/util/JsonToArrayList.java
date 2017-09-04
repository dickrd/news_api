package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by first1hand on 2017/4/12.
 */
public class JsonToArrayList {
    public  static ArrayList<JsonObject> jsonToArrayList(String json)
    {
        Type type = new TypeToken<ArrayList<JsonObject>>()
        {}.getType();
        return new Gson().fromJson(json, type);
    }
}
