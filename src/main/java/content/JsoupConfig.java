package content;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class JsoupConfig {

    private static final Logger logger = Logger.getLogger(JsoupConfig.class.getName());
    private static final String path = "selectors.json";

    private static Gson gson = new Gson();
    private static HashMap<String, JsoupContentParser.Selectors> selectorMap = new HashMap<>();

    public static JsoupContentParser.Selectors getSelectors(String name) {
        if (selectorMap.isEmpty()) {
            try {
                JsoupContentParser.Selectors[] selectorsArray = gson.fromJson(new FileReader(path),
                        new TypeToken<JsoupContentParser.Selectors[]>(){}.getType());
                for (JsoupContentParser.Selectors selectors: selectorsArray) {
                    selectorMap.put(selectors.name, selectors);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Config error!", e);
            }
        }

        return selectorMap.get(name);
    }
}
