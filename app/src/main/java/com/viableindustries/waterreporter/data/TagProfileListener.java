package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.OrganizationProfileActivity;

/**
 * Created by brendanmcintyre on 4/12/17.
 */

public class TagProfileListener implements View.OnClickListener {

        private Context context;

        private HashTag hashTag;

        public TagProfileListener(Context context, HashTag hashTag) {
            this.context = context;
            this.hashTag = hashTag;
        }

        @Override
        public void onClick(View view) {

            Intent intent = new Intent(context, OrganizationProfileActivity.class);

            TagHolder.setHashTag(hashTag);

            context.startActivity(intent);

        }
}
