package com.cch.scrollviewhelper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ³¿êÍ on 2015-09-04.
 */
public class FragmentText extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View consertview = inflater.inflate(R.layout.fragment_test,null);
        consertview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ViewPager vp = (ViewPager) consertview.findViewById(R.id.vp_test);
        vp.setAdapter(new ViewpagerAdapter(getActivity()));
        return consertview;
    }
}
