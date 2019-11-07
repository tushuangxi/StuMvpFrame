package com.config.pad.content.libding.rerxmvp.interfaceUtils;

import com.config.pad.content.libding.entry.GetListRsp;

import rx.Observable;
import rx.Subscriber;

public class interfaceUtilsAll {

    public interface Ipresenter<V extends IView> {
        /**
         * 关联P与V
         * @param v
         */
        void attachView(V v);

        /**
         * 取消关联P与V
         */
        void detachView();

        /**
         * Rx订阅
         */
        void subscribe(Observable observable, Subscriber subscriber);

        /**
         * Rx取消订阅
         */
        void unSubscribe();
    }

    public interface IView {

        void showLoading();

        void hideLoading();
    }


    //获取数据  GetListRsp
    public interface GetListRspView extends IView {
        void getGetListRsp(GetListRsp getListRsp);
    }

}
