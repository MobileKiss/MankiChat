package by.mobilekiss.mankichat;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class My_Friends_Adapter extends ArrayAdapter<String>{
    private final Context context;
    private final ArrayList<String> values;
   

    public My_Friends_Adapter(Context context, ArrayList<String> values) {
        super(context, R.layout.my_friends_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	View view = convertView;
    	if (view == null){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.my_friends_list_item, parent, false);
        ViewHolder hld = new ViewHolder();
        hld.tvSender = (TextView) view.findViewById(R.id.tv_name_myfriends_list_item);
        hld.tvSender.setTypeface(FontFactory.getUbuntuBold(context));
        
        hld.tvDate = (TextView) view.findViewById(R.id.tv_phone_myfriends_list_item);
        hld.tvDate.setTypeface(FontFactory.getUbuntuNorm(context));
        view.setTag(hld);
    	}
        
        ViewHolder hld = (ViewHolder) view.getTag();
        String str = values.get(position).toString();
        
        hld.tvSender.setText(str.subSequence(0, str.indexOf("\n")));
        hld.tvDate.setText(str.subSequence(str.indexOf("\n") + 1, str.length()));
       	       
        return view;
    }

	
}