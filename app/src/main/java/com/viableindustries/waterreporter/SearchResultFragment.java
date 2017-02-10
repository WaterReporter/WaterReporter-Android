package com.viableindustries.waterreporter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by brendanmcintyre on 2/10/17.
 */

public class SearchResultFragment extends Fragment {

    // Store instance variables

    private int instructional_text;

    private int image_one, image_two, image_three;

    private int caption_one_title, caption_two_title, caption_three_title;

    private int caption_one_body, caption_two_body, caption_three_body;

    // newInstance constructor for creating fragment with arguments
    public static SearchResultFragment newInstance(JSONObject params) {

        SearchResultFragment fragmentFirst = new SearchResultFragment();

        Bundle args = new Bundle();

        Iterator<?> keys = params.keys();

        Log.v("keys", keys.toString());

        while (keys.hasNext()) {

            String key = (String) keys.next();

            try {

                args.putInt(key, params.getInt(key));

            } catch (JSONException e) {

                e.printStackTrace();

            }

        }

        fragmentFirst.setArguments(args);

        return fragmentFirst;

    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        instructional_text = getArguments().getInt("instructional_text", -1);

        image_one = getArguments().getInt("image_one", -1);

        image_two = getArguments().getInt("image_two", -1);

        image_three = getArguments().getInt("image_three", -1);

        caption_one_title = getArguments().getInt("caption_one_title", -1);

        caption_two_title = getArguments().getInt("caption_two_title", -1);

        caption_three_title = getArguments().getInt("caption_three_title", -1);

        caption_one_body = getArguments().getInt("caption_one_body", -1);

        caption_two_body = getArguments().getInt("caption_two_body", -1);

        caption_three_body = getArguments().getInt("caption_three_body", -1);

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_result_list, container, false);

        ListView resultList = (ListView) view.findViewById(R.id.resultList);

        return view;

    }

}
