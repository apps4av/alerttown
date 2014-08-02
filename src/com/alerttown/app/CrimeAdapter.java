package com.alerttown.app;


import java.lang.reflect.Field;
import java.util.Locale;

import com.alerttown.app.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author zkhan
 *
 */
public class CrimeAdapter extends ArrayAdapter<String> {

    private Context  mContext;
    private String[] mType;
    
    Bitmap mBitmaps[];
    
    /**
     * Get an icon resource ID from name of crime
     * @param pos
     * @return
     */
    public static int getIconResource(String pos) {
        
        // This is needed for images names in resources
        pos = pos.replace(" ", "_").toLowerCase(Locale.US);
        try {
            Class<R.drawable> res = R.drawable.class;
            Field field = res.getField(pos);
            return field.getInt(null);
        }
        catch (Exception e) {
        }
        return -1;
    }

    /**
     * 
     * @param context
     * @param type
     */
    public CrimeAdapter(Context context, String[] type) {
        super(context, R.layout.report, type);
        mContext = context;
        mType = type;
        
        mBitmaps = new Bitmap[type.length];

        for(int i = 0; i < mType.length; i++) {
            mBitmaps[i] = BitmapFactory.decodeResource(context.getResources(),
                    getIconResource(mType[i]));
        }
    }

    
    /**
     * 
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;

        if(null == rowView) {
            rowView = inflater.inflate(R.layout.crime_list, parent, false);
        }
        TextView textView = (TextView)rowView.findViewById(R.id.crime_list_text);
        textView.setText(mType[position]);
        
        ImageView img = (ImageView)rowView.findViewById(R.id.crime_list_img);
        
        Bitmap b = getBitmap(mType[position]);
        img.setImageBitmap(b);

        return rowView;
    }

    /**
     * Get a Bitmap instance based on crime name
     * @param pos
     * @return
     */
    public Bitmap getBitmap(String pos) {

        /*
         * Compare and send
         */
        for(int i = 0; i < mType.length; i++) {
            if(mType[i].equals(pos)) {
                return mBitmaps[i];
            }
        }
        
        return null;
    }   
}
