package aa.pmnote;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by anton.gorielikov on 8/30/2017.
 */

public class MyArrayAdapter extends ArrayAdapter<String> {

    private View.OnLongClickListener mOLCL = null;

    public MyArrayAdapter(Context context, int res, ArrayList<String> list)
    {
        super(context, res, list);
    }

    public void SetOLCL(View.OnLongClickListener olcl){
        mOLCL = olcl;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        if (convertView == null && mOLCL != null) { //set listener only for newly created view
            view.setLongClickable(true);
            view.setOnLongClickListener(mOLCL);
        }
        return view;
    }
}
