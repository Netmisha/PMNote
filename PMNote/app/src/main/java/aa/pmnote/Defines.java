package aa.pmnote;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        OTHER,
        EXPIRED,
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
    final static String LISTS_FOLDER = "Lists";

    final static String LIST_TASKS = TASKS_FOLDER;
    final static String LIST_PLACEHOLDER = "placeholder";

    final static String TASK_STATUS = "Status";
    final static String TASK_DATE = "Date";
    final static String TASK_TIME = "Time";
    final static String TASK_DESCR = "Description";
    final static String TASK_ATTACHED_PEOPLE = PEOPLE_FOLDER;
    final static String TASK_ATTACHED_PROJECTS = PROJECTS_FOLDER;
    final static String TASK_ATTACHED_LISTS = LISTS_FOLDER;

    final static String PROJECT_PLACEHOLDER = "placeholder";
    final static String PROJECT_TASKS = TASKS_FOLDER;
    final static String PROJECT_PEOPLE = PEOPLE_FOLDER;

    final static String PERSON_TASKS = TASKS_FOLDER;
    final static String PERSON_PLACEHOLDER = "None";
    final static String PERSON_PROJECTS = PROJECTS_FOLDER;
    final static String PERSON_INFO = "Info";
    final static String PERSON_INFO_EMAIL = "email";

    private static ArrayList<String> taskOptions = new ArrayList<>();
    private static ArrayList<String> projectsOptions = new ArrayList<>();

    final static String OPEN_TASKS = "Open";
    final static String COMPLETED_TASKS = "Completed";
    final static String NEW_LIST = "New list";

    final static InputFilter NAME_FILTER = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start;i < end;i++) {
                if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.toString(source.charAt(i)).equals("_") && !Character.toString(source.charAt(i)).equals("-")
                        && !Character.toString(source.charAt(i)).equals(" ")) {
                    return "";
                }
            }
            return null;
        }
    };

    static void SetArrayList(final ArrayList<String> list, DataSnapshot listFolder) {
        list.clear();
        list.add(OPEN_TASKS);
        list.add(COMPLETED_TASKS);
        for (DataSnapshot ds : listFolder.getChildren()) {
            list.add(ds.getKey());
        }
        list.add(NEW_LIST);
    }

    static void SetArrayList(ArrayList<String> list, Defines.LinearLayoutType llt)
    {
        list.clear();
        list.add("Show All");
        list.add("Show Projects");
        list.add("Show People");
    }

    static boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    static String hlinkFromEmail(String email)
    {
        return "<a href=\""+email+"\">" + email+"</a>";
    }

    static boolean isListGone(LinearLayout ll) {
        boolean result = true;
        if(ll.getChildCount() != 2) {
            for (int i = 2; i < ll.getChildCount(); i += Defines.ITEM_SIZE_IN_VIEWS) {
                if (ll.getChildAt(i).getVisibility() != View.GONE) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}
