package crashavoidance.se.rit.datacollector.persistence;

import android.provider.BaseColumns;

/**
 * Created by Chris on 6/12/2016.
 */
public class StepTimerContract {
    public StepTimerContract(){}

    public static abstract class StepEntry implements BaseColumns {
        public static final String TABLE_NAME = "Step_Timer";
        public static final String COLUMN_NAME_PHONE_ID = "phone_id";
        public static final String COLUMN_NAME_STEP_NAME = "step_name";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_STATUS = "status";
    }

}