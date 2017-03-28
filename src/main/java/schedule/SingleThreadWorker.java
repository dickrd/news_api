package schedule;

import content.JsoupContentParser;
import content.Record;
import download.GenericDownloader;
import storage.CsvFileStorage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/28/2017.
 * A worker that performs download and parse task in a single thread.
 */
public class SingleThreadWorker {

    private static final Logger logger = Logger.getLogger(SingleThreadWorker.class.getName());

    private GenericDownloader downloader;
    private CsvFileStorage csvFileStorage;

    public SingleThreadWorker() {
        try {
            downloader = new GenericDownloader();
            csvFileStorage = new CsvFileStorage();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing!", e);
        }
    }

    public void feeds(String[] keywords) {
        for (String keyword : keywords) {
            try {
                String searchResult = downloader.search(keyword);
                List<String> links = JsoupContentParser.parseLinks(searchResult, "", "");

                for (String link : links) {
                    String download = downloader.download(link);
                    JsoupContentParser.parseRecord(download, link, selectors);
                }

                csvFileStorage.store();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Keyword failed.", e);
            }
        }
    }
}
