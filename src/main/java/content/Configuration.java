package content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class Configuration {

    private static final Logger logger = Logger.getLogger(Configuration.class.getName());
    private static final String path = "selectors.json";
    private static final String nameField = "name";

    private static Gson gson = new Gson();
    private static HashMap<String, JsonObject> selectorMap = new HashMap<>();

    public static String getSelector(String name, String field) {
        if (selectorMap.isEmpty()) {
            try {
                JsonObject[] selectorsArray = gson.fromJson(new FileReader(path),
                        new TypeToken<JsonObject[]>(){}.getType());
                for (JsonObject selectors: selectorsArray) {
                    String aName = selectors.get(nameField).getAsString();
                    selectorMap.put(aName, selectors);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Config error!", e);
            }
        }

        try {
            JsonObject theObject = selectorMap.get(name);
            return theObject.get(field).getAsString();
        } catch (Exception e) {
            logger.log(Level.WARNING, "No such field or name: " + name + ", " + field, e);
            return "";
        }
    }
}
