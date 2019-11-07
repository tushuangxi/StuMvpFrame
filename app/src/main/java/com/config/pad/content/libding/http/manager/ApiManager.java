package com.config.pad.content.libding.http.manager;

import com.config.pad.content.libding.http.ApiService;
import com.config.pad.content.libding.http.AppConfig;
import com.config.pad.content.libding.utils.JsonHandleUtils;
import com.config.pad.content.libding.utils.LogUtils;
import com.config.pad.content.libding.utils.NetworkUtils;
import com.config.pad.content.libding.application.PadApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
  * 单例的Retrofit和Okhttp管理类
 */
public class ApiManager {
    private static final String TAG = "ApiManager";
    private static ApiManager mApiManager;
    private OkHttpClient mOkHttpClient;
    private static ApiService mApiService;
    private final int TIMEOUT = 10;

    /**
     * 请求数据缓存    存储路径配置
     */
    private  final String HTTP_CACHE = PadApplication.getContext().getCacheDir() + "/httpCache";

    private ApiManager() {
        initOkhttp();
        initRetrofit();
    }

    public static synchronized ApiService getApiService() {
        if (mApiService == null) {
            mApiManager = new ApiManager();
        }
        return mApiService;
    }

    private void initOkhttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)  //连接超时设置
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)  //写入缓存超时10s
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)  //读取缓存超时10s
                .retryOnConnectionFailure(true)  //失败重连
                .addInterceptor(headerInterceptor)  //添加header
                .addInterceptor(netCacheInterceptor);  //添加网络缓存

                addLogIntercepter(builder);  //日志拦截器
                setCacheFile(builder);  //网络缓存

        mOkHttpClient = builder.build();
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.SERVER_ADDRESS)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .build();

        mApiService = retrofit.create(ApiService.class);
    }

    /**
     * 设置缓存文件路径
     */
    private void setCacheFile(OkHttpClient.Builder builder) {
        //设置缓存文件
        File cacheFile = new File(HTTP_CACHE);
        //缓存大小为100M
        int cacheSize = 100 * 1024 * 1024;
        Cache cache = new Cache(cacheFile,cacheSize);
        builder.cache(cache);
    }

    /**
     * 调试模式下加入日志拦截器
     * @param builder
     */
    private void addLogIntercepter(OkHttpClient.Builder builder) {
        if (AppConfig.isDebug) {
            builder.addInterceptor(mLoggingInterceptor); // loggingInterceptor
        }
    }

    /**
     *  网络拦截器进行网络缓存
     */
    private  Interceptor netCacheInterceptor = new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            int onlineCacheTime = 60;

            return response.newBuilder()
                    .header("Cache-Control", "public, max-age="+onlineCacheTime)
                    .removeHeader("Pragma")
                    .build();
        }
    };

    /**
     * 统一添加header的拦截器
     */
    private Interceptor headerInterceptor = new  Interceptor() {

        //缓存有效期 1天
        private static final long CACHE_STALE_SECOND = 24 * 60 * 60;

 /*   @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("token", UserInfoCache.TOKEN);

        return chain.proceed(builder.build());
    }*/

        // server响应头拦截器，用来配置缓存策略
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetworkUtils.isConnected(PadApplication.getContext())) {
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
//            LogUtils.e(TAG, "no network");
            }
            Response originalResponse = chain.proceed(request);

            if (NetworkUtils.isConnected(PadApplication.getContext())) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .header("Content-Type", "application/json")
                        .removeHeader("Pragma").build();
            } else {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached," + CACHE_STALE_SECOND)
                        .removeHeader("Pragma").build();
            }
        }
    };

    /**
     * Okhttp返回数据日志拦截器
     */
    public Interceptor loggingInterceptor = new Interceptor() {
        private final int byteCount = 1024*1024;

        @Override
        public Response intercept(Chain chain) throws IOException {
            //chain里面包含了request和response，按需获取
            Request request = chain.request();
            LogUtils.d("-----------------------开始打印请求数据-----------------------");
            Response response = chain.proceed(request);

            LogUtils.d(String.format("发送请求  %s",request.url()));
            ResponseBody responseBody = response.peekBody(byteCount);
            LogUtils.d(String.format("接收响应  %s", responseBody.string()));
            LogUtils.d("-----------------------结束打印请求数据-----------------------");
            return response;
        }
    };

    // 打印json数据拦截器
    private Interceptor mLoggingInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            //request
            final Request request = chain.request();
            LogUtils.v("-----------------------开始打印请求数据-----------------------");
            if (request != null) {
                LogUtils.w("发送请求:" + request.toString());
                Headers headers = request.headers();
                if (headers != null) {
                    LogUtils.d("headers : " + headers.toString());

                }
                RequestBody body = request.body();
                if (body != null) {
                    Buffer buffer = new Buffer();
                    body.writeTo(buffer);
                    String req = buffer.readByteString().utf8();
                    LogUtils.e("接收响应:" + "body : " + req);
                }

            }
            LogUtils.d("-----------------------结束打印请求数据-----------------------");

            //response
            final Response response = chain.proceed(request);
            final ResponseBody responseBody = response.body();
            final long contentLength = responseBody.contentLength();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(charset);
                } catch (UnsupportedCharsetException e) {
                    LogUtils.d( "Couldn't decode the response body; charset is likely malformed.");
                    return response;

                }
            }
            if (contentLength != 0) {
                LogUtils.d( "-----------------------开始打印响应数据-----------------------");
                LogUtils.e("响应数据:" + buffer.clone().readString(charset));

                LogUtils.d( "-----------------------结束打印响应数据-----------------------");
            }

            //retrofit   illegalStateException:closed     responseBody.string()必须注释掉 否则报错
            String responseString = JsonHandleUtils.jsonHandle(buffer.clone().readString(charset));
//            LogUtils.d("JsonData--->拦截器" + responseString.toString()+"\n \n");

            return response;

        }
    };
}