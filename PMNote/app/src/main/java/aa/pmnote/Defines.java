package aa.pmnote;

import java.util.ArrayList;

/**
 * Created by anton.gorielikov on 8/23/2017.
 */

public class Defines {
    enum LinearLayoutType {
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

    final static int PROJECTS_FRAGMENT = 0;
    final static int TASKS_FRAGMENT = 1;

    final static int PROJECT_FRAGMENT = 0;
    final static String PROJECT_FRAGMENT_TITLE = "Project ";
    final static int PROJECT_TASKS_FRAGMENT = 1;
    final static String PROJECT_TASKS_FRAGMENT_TITLE = " tasks";

    final static int TEXT_VIEW_POSITION = 1;
    final static int ITEM_SIZE_IN_VIEWS = 2;

    final static String INFO_TAG = "PERSON_NAME";

    final static String USERS_FOLDER = "Users";
    final static String PEOPLE_FOLDER = "People";
    final static String TASKS_FOLDER = "Tasks";
    final static String PROJECTS_FOLDER = "Projects";

    final static String TASK_STATUS = "Status";
    final static String TASK_DATE = "Date";
    final static String TASK_TIME = "Time";
    final static String TASK_DESCR = "Description";
    final static String TASK_ATTACHED_PEOPLE = PEOPLE_FOLDER;
    final static String TASK_ATTACHED_PROJECTS = PROJECTS_FOLDER;

    final static String PROJECT_PLACEHOLDER = "placeholder";
    final static String PROJECT_TASKS = TASKS_FOLDER;
    final static String PROJECT_PEOPLE = PEOPLE_FOLDER;

    final static String PERSON_TASKS = TASKS_FOLDER;
    final static String PERSON_PLACEHOLDER = "None";
    final static String PERSON_PROJECTS = PROJECTS_FOLDER;

    static void SetArrayList(ArrayList<String> list, Defines.LinearLayoutType llt)
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
