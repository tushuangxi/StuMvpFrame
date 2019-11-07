package com.config.pad.content.libding.rerxmvp.presenter;

import com.config.pad.content.libding.rerxmvp.base.BasePresenter;
import com.config.pad.content.libding.http.ServiceMapParams;
import com.config.pad.content.libding.rerxmvp.interfaceUtils.interfaceUtilsAll;
import com.config.pad.content.libding.rerxmvp.model.GetAllDataListModelImpl;
import com.config.pad.content.libding.entry.GetListRsp;


/**
 * create by libo
 * create on 2018/12/27
 * description
 */
public class GetListRspPresenter extends BasePresenter<interfaceUtilsAll.GetListRspView> {

    public GetListRspPresenter(interfaceUtilsAll.GetListRspView getListRspView) {
        attachView(getListRspView);
    }

    public void getData() {
        subscribe(apiService.requestGetMvpRspList( ServiceMapParams.getGetListRspMapParams()), new GetAllDataListModelImpl().new ApiCallBack() {
            @Override
            public void onSuccess(GetListRsp getListRsp) {
                view.getGetListRsp(getListRsp);
            }
        });
    }
}
