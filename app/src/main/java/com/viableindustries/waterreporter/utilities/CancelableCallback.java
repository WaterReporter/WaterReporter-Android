//package com.viableindustries.waterreporter.utilities;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import retrofit.Callback;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
///**
// * Created by brendanmcintyre on 9/9/17.
// */
//
//public abstract class Callback<T> implements Callback<T> {
//
//    private static final List<Callback> mList = new ArrayList<>();
//
//    private boolean isCanceled = false;
//    private Object mTag = null;
//
//    public static void cancelAll() {
//        Iterator<Callback> iterator = mList.iterator();
//        while (iterator.hasNext()){
//            iterator.next().isCanceled = true;
//            iterator.remove();
//        }
//    }
//
//    public static void cancel(Object tag) {
//        if (tag != null) {
//            Iterator<Callback> iterator = mList.iterator();
//            Callback item;
//            while (iterator.hasNext()) {
//                item = iterator.next();
//                if (tag.equals(item.mTag)) {
//                    item.isCanceled = true;
//                    iterator.remove();
//                }
//            }
//        }
//    }
//
//    public Callback() {
//        mList.add(this);
//    }
//
//    public Callback(Object tag) {
//        mTag = tag;
//        mList.add(this);
//    }
//
//    public void cancel() {
//        isCanceled = true;
//        mList.remove(this);
//    }
//
//    @Override
//    public final void success(T t, Response response) {
//        if (!isCanceled)
//            onSuccess(t, response);
//        mList.remove(this);
//    }
//
//    @Override
//    public final void failure(RetrofitError error) {
//        if (!isCanceled)
//            onFailure(error);
//        mList.remove(this);
//    }
//
//    public abstract void onSuccess(T t, Response response);
//
//    public abstract void onFailure(RetrofitError error);
//}
