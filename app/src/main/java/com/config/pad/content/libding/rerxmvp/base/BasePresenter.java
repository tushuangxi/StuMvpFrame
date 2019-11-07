package com.config.pad.content.libding.rerxmvp.base;

import com.config.pad.content.libding.http.manager.ApiManager;
import com.config.pad.content.libding.http.ApiService;
import com.config.pad.content.libding.rerxmvp.interfaceUtils.interfaceUtilsAll;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * create by libo
 * create on 2018/11/13
 * description presenter基类，绑定observable与subscriber
 */
public class BasePresenter<V extends interfaceUtilsAll.IView> implements interfaceUtilsAll.Ipresenter<V> {
    protected V view;
    protected ApiService apiService;
    protected CompositeSubscription mCompositeSubscription;

    @Override
    public void attachView(interfaceUtilsAll.IView v) {
        this.view = (V) v;
        apiService = ApiManager.getApiService();
    }

    @Override
    public void detachView() {
        this.view = null;
        unSubscribe();
    }

    @Override
    public void subscribe(Observable observable, Subscriber subscriber) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }

        //绑定observable与subscriber
        Subscription subscription = observable.observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(Schedulers.io())
                  .subscribe(subscriber);

        mCompositeSubscription.add(subscription);
    }

    @Override
    public void unSubscribe() {
        if (mCompositeSubscription != null && mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }
}