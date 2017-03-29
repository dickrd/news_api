package schedule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
class SingleThreadWorkerTest {

    private SingleThreadWorker singleThreadWorker = new SingleThreadWorker();

    @Test
    void feeds() {
        singleThreadWorker.feeds(new String[]{"回家", "大数据"});
    }

}