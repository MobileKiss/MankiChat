package by.mobilekiss.mankichat;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessangerAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> values;

	public MessangerAdapter(Context context, ArrayList<String> values) {
		super(context, R.layout.messages_list_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.messages_list_item, parent, false);
			ViewHolder hld = new ViewHolder();
			hld.tvSender = (TextView) view.findViewById(R.id.tvSender);
			hld.tvDate = (TextView) view.findViewById(R.id.tvDate);
			hld.imageView = (ImageView) view
					.findViewById(R.id.ivStatusImage);

			hld.tvDate.setTypeface(FontFactory.getUbuntuNorm(context));
			hld.tvSender.setTypeface(FontFactory.getUbuntuBold(context));

			view.setTag(hld);
		}
		ViewHolder hld = (ViewHolder) view.getTag();
		String str = values.get(position).toString();

		hld.tvSender.setText(str.subSequence(0, str.indexOf("\n")));
		
		String tempDate = (String)str.subSequence(str.indexOf("\n") + 1, str.length());
		if (str.subSequence(str.indexOf("\n") + 1, str.length()).toString()
				.contentEquals("null"))
			hld.tvDate.setText("");
		else
			hld.tvDate.setText(str.subSequence(str.indexOf("\n") + 1,
					str.length()));

		String s = values.get(position).toString();
		if (s.contains(" - новое сообщение")) {

			hld.imageView.setImageResource(R.drawable.messages_form_new_message);
			hld.tvSender.setTextColor(Color.parseColor("#6666CC"));
			view.setBackgroundColor(Color.parseColor("#EFEFFA"));

		}

		if (s.contains(" - просмотрено")) {
			hld.tvSender.setTextColor(Color.parseColor("#999999"));
			hld.imageView
					.setImageResource(R.drawable.messages_form_opened_message);
			view.setBackgroundColor(0);
			

		}

		if (s.contains("отправлено")) {
			hld.tvSender.setTextColor(Color.parseColor("#999999"));
			hld.imageView
					.setImageResource(R.drawable.messages_form_outcoming_message);
			view.setBackgroundColor(0);
		}
		if (s.contains(" - зарегистрировался")) {
			hld.tvSender.setTextColor(Color.parseColor("#999999"));
			hld.imageView
					.setImageResource(R.drawable.messages_form_new_friend_message);

		}

		return view;
	}

}