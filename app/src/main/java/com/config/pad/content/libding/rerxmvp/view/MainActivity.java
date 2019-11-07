package com.config.pad.content.libding.rerxmvp.view;

import android.os.Bundle;
import android.widget.TextView;
import com.config.pad.content.libding.rerxmvp.base.BaseMvpActivity;
import com.config.pad.content.R;
import com.config.pad.content.libding.rerxmvp.interfaceUtils.interfaceUtilsAll;
import com.config.pad.content.libding.rerxmvp.presenter.GetListRspPresenter;
import com.config.pad.content.libding.entry.GetListRsp;


public class MainActivity extends BaseMvpActivity<GetListRspPresenter> implements interfaceUtilsAll.GetListRspView {
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvContent = findViewById(R.id.tv_content);
        mPresenter.getData();
    }

    @Override
    protected GetListRspPresenter createPresenter() {
        return new GetListRspPresenter(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void getGetListRsp(GetListRsp getListRsp) {
        tvContent.setText(getListRsp.getFemale().toString());
    }
}
