package by.mobilekiss.mankichat;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


public class PostAdapter extends ArrayAdapter<String>{
    private final Context context;
    private final ArrayList<String> values;
    CheckBox cbphone;
    TextView tvSender, tvNickname;
    ArrayList<Integer> selItems;
    
    public PostAdapter(Context context, ArrayList<String> values, ArrayList<Integer> selItems) {
        super(context, R.layout.messages_list_item, values);
        this.context = context;
        this.values = values;
        this.selItems = selItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	View view = convertView;
    	if (view == null){
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        view = inflater.inflate(R.layout.post_list_item, parent, false);
	        ViewHolder hld = new ViewHolder();
	        hld.cbphone = (CheckBox) view.findViewById(R.id.post_list_phonenumber);
	        hld.cbphone.setTag(String.valueOf(position));
	        
	        hld.tvSender = (TextView) view.findViewById(R.id.post_list_tvPhone);
	        hld.tvSender.setTypeface(FontFactory.getUbuntuNorm(context));
	        hld.tvSender.setTag(String.valueOf(position));
	        
	        hld.tvDate =  (TextView) view.findViewById(R.id.post_list_nickname);
	        hld.tvDate.setTag(String.valueOf(position));
	        hld.tvDate.setTypeface(FontFactory.getUbuntuBold(context));
	        view.setTag(hld);
    	}
        
        ViewHolder hld = (ViewHolder) view.getTag();
        String str = values.get(position).toString();
        
        hld.cbphone.setChecked(selItems.get(position)!=0);
        hld.cbphone.setTag(String.valueOf(position));
        hld.tvDate.setText(str.subSequence(0, str.indexOf("\n")));
        hld.tvDate.setTag(String.valueOf(position));
        hld.tvSender.setText(str.substring(str.indexOf("\n") + 1, str.length()));
        hld.tvSender.setTag(String.valueOf(position));
             
        return view;
    }
    
}