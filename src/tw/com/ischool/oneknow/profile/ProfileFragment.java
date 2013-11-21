package tw.com.ischool.oneknow.profile;

import tw.com.ischool.oneknow.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ProfileFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container,
				false);

		Bitmap bmp = BitmapFactory.decodeResource(this.getActivity()
				.getResources(), R.drawable.profile_background);
		int bmpwidth = bmp.getWidth();
		int bmpheight = bmp.getHeight();
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		
		float f = bmpwidth / screenWidth;
		int newheight = (int)(bmpheight / f);
		
		ImageView imageView = new ImageView(this.getActivity());
				
		Matrix matrix = new Matrix();
		matrix.postScale(screenWidth,newheight);
		Bitmap bm = Bitmap.createBitmap(bmp,0,0,bmpwidth,bmpheight ,matrix,true);
		imageView.setImageBitmap(bm);
		
		//RelativeLayout layout = (RelativeLayout)view.findViewById(R.id.layout_profile_header);		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(bmpwidth,bmpheight);
		
		imageView.setLayoutParams(params);
		
		return view;
	}
}
