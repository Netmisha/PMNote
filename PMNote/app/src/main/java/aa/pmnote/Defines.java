package aa.pmnote;

import java.util.ArrayList;

/**
 * Created by anton.gorielikov on 8/23/2017.
 */

public class Defines {
    public enum LinearLayoutType {
        PROJECT,
        PERSON,
        TASK
    }

    public enum TaskType
    {
        TODAY,
        WEEK,
        MONTH,
        OTHER
    }

    public final static int PROJECTS_FRAGMENT = 0;
    public final static int TASKS_FRAGMENT = 1;

    public final static int PROJECT_FRAGMENT = 0;
    public final static String PROJECT_FRAGMENT_TITLE = "Project ";
    public final static int PROJECT_TASKS_FRAGMENT = 1;
    public final static String PROJECT_TASKS_FRAGMENT_TITLE = " tasks";

    public final static int TEXT_VIEW_POSITION = 1;
    public final static int ITEM_SIZE_IN_VIEWS = 2;

    public final static String INFO_TAG = "PERSON_NAME";

    public static void SetArrayList(ArrayList<String> list, Defines.LinearLayoutType llt)
    {
        list.clear();
        if(llt == Defines.LinearLayoutType.TASK)
        {
            list.add("Show All");
            list.add("Show Open");
            list.add("Show Completed");
        }
        else
        {
            list.add("Show All");
            list.add("Show Projects");
            list.add("Show People");
        }
    }
}
