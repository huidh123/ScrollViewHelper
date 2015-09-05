package com.cch.scrollviewhelper;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by ³¿êÍ on 2015-09-04.
 */
public class ViewpagerAdapter extends PagerAdapter {

    private int [] picIds = new int[]{R.drawable.pic1, R.drawable.pic2,R.drawable.pic3};
    Context context;
    private ArrayList<View> viewsList;
    public ViewpagerAdapter(Context context){
        this.context = context;
        viewsList = new ArrayList<View>();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        for(int picId : picIds){
            View tempView = layoutInflater.inflate(R.layout.item_pic_layout,null);
            ImageView picImgv = (ImageView) tempView.findViewById(R.id.imgv_pic);
            picImgv.setImageDrawable(context.getResources().getDrawable(picId));
            viewsList.add(tempView);
        }
    }
    @Override
    public int getCount() {
        return picIds.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(View view, int position, Object object)                       //Ïú»ÙItem
    {
        ((ViewPager) view).removeView(viewsList.get(position));
    }

    @Override
    public Object instantiateItem(View view, int position)                                //ÊµÀý»¯Item
    {
        ((ViewPager) view).addView(viewsList.get(position), 0);
        return viewsList.get(position);
    }
}
