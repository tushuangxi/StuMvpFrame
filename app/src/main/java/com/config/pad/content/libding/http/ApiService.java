package com.config.pad.content.libding.http;

import com.config.pad.content.libding.entry.GetListRsp;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import static com.config.pad.content.libding.http.AppConfig.isDebug;

/**
 * create by libo
 * create on 2018/11/13
 * description Retrofit调用接口方法
 */
public interface ApiService {

    /** *******************************************  IP配置  ******************************************** */

    /** 正式服务器地址 */
    String SERVER_ADDRESS_RELEASE = "http://api.zhuishushenqi.com";

    /** 测试服务器地址 */
    String SERVER_ADDRESS_DEBUG = "http://api.zhuishushenqi.com";

    /** 服务器域名 */
    String SERVER_ADDRESS = isDebug ? SERVER_ADDRESS_DEBUG : SERVER_ADDRESS_RELEASE;

    @GET("/cats/lv2/statistics/")
    Observable<GetListRsp> requestGetMvpRspList(@QueryMap Map<String, String> params);
}
