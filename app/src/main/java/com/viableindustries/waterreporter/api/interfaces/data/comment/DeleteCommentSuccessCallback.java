package com.viableindustries.waterreporter.api.interfaces.data.comment;

import com.viableindustries.waterreporter.api.models.comment.Comment;

/**
 * Created by brendanmcintyre on 10/17/17.
 */

public interface DeleteCommentSuccessCallback {

    void onCommentDelete(Comment comment);

}
