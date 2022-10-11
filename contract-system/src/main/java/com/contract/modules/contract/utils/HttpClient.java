package com.contract.modules.contract.utils;

import com.contract.config.thread.ThreadPoolExecutorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/11
 * @Version: 1.0
 * @Description:
 */
@Slf4j
public final class HttpClient {

    /**
     * 发送get请求
     *
     * @param url 请求地址，可以拼接参数
     */
    public static HttpResponse<String> get(String url) {
        return get(url, null);
    }

    /**
     * 发送get请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    public static HttpResponse<String> get(String url, Map<String, Object> params) {
        return get(url, params, null, -1, true, compressedBodyHandler);
    }
    /**
     * 发送get请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    public static HttpResponse<String> get(String url, Map<String, Object> params, Map<String, String> headers) {
        return get(url, params, headers, -1, true, compressedBodyHandler);
    }

    /**
     * 发送get异步请求
     *
     * @param url 请求地址，可以拼接参数
     */
    public static CompletableFuture<HttpResponse<String>> getAsync(String url) {
        return getAsync(url, null);
    }

    /**
     * 发送get异步请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    public static CompletableFuture<HttpResponse<String>> getAsync(String url, Map<String, Object> params) {
        return getAsync(url, params, null, -1, true, compressedBodyHandler);
    }
    /**
     * 发送get异步请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    public static CompletableFuture<HttpResponse<String>> getAsync(String url, Map<String, Object> params, Map<String, String> headers) {
        return getAsync(url, params, headers, -1, true, compressedBodyHandler);
    }

    /**
     * 下载文件
     * <br/>
     * 若确定服务器可以响应Content-Disposition: attachment; filename=a.xx
     * 那么fileName文件名可以不传递,否则必须传递fileName
     *
     * @param url       请求路径
     * @param directory 保存的文件目录
     * @param fileName  文件名称 可以不传递
     * @param timeOut   超时时间 秒
     */
    public static HttpResponse<Path> downLoad(String url, String directory, String fileName, int timeOut) {
        HttpResponse.BodyHandler<Path> bodyHandler;
        if (!Files.isDirectory(Path.of(directory))) {
            throw new RuntimeException("不是一个目录: " + directory);
        }
        if (fileName == null || fileName.isBlank()) {
            bodyHandler = HttpResponse.BodyHandlers.ofFileDownload(Path.of(directory), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } else {
            bodyHandler = HttpResponse.BodyHandlers.ofFile(Path.of(directory, fileName));
        }
        return get(url, null, null, timeOut, false, bodyHandler);
    }

    /**
     * 发送get请求，url和参数map的key及value不能urlEncode
     *
     * @param url                 请求地址
     * @param params              请求参数map
     * @param headers             请求头map
     * @param timeOut             超时时间 秒
     * @param gzip                启用gzip
     * @param responseBodyHandler HttpResponse.BodyHandler
     */
    public static <T> HttpResponse<T> get(String url, Map<String, Object> params, Map<String,
            String> headers, int timeOut, boolean gzip, HttpResponse.BodyHandler<T> responseBodyHandler)
    {

        HttpRequest request = ofGetHttpRequest(url, params, headers, timeOut, gzip);
        try {
            return DEFAULT_HTTP_CLIENT.send(request, responseBodyHandler);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送get异步请求，url和参数map的key及value不能urlEncode
     *
     * @param url                 请求地址
     * @param params              请求参数map
     * @param headers             请求头map
     * @param timeOut             超时时间 秒
     * @param gzip                启用gzip
     * @param responseBodyHandler HttpResponse.BodyHandler
     */
    public static <T> CompletableFuture<HttpResponse<T>> getAsync(String url, Map<String, Object> params, Map<String,
            String> headers, int timeOut, boolean gzip, HttpResponse.BodyHandler<T> responseBodyHandler) {

        HttpRequest request = ofGetHttpRequest(url, params, headers, timeOut, gzip);
        return DEFAULT_HTTP_CLIENT.sendAsync(request, responseBodyHandler);
    }

    private static HttpRequest ofGetHttpRequest(String url, Map<String, Object> params, Map<String, String> headers,
                                                int timeOut, boolean gzip) {
        if (params != null && !params.isEmpty()) {
            String strip = url.strip();
            url = strip.endsWith("/") ? strip.substring(0, strip.length() - 1) : strip;
            String q = "?";
            String link = "&";
            int i = url.indexOf(q);
            if (i != -1 && i != url.length() - 1) {
                String paramsStr = url.substring(i + 1);
                for (String s : paramsStr.split(link)) {
                    String[] pair = s.split("=");
                    if (pair.length == 2) {
                        params.put(pair[0], pair[1]);
                    }
                }
                url = url.substring(0, i);
            }

            String queryStr = mapToQueryString(params, true);
            if (!queryStr.isEmpty()) {
                String prefix = "";
                if (!url.endsWith(q) && !url.endsWith(link)) {
                    prefix = q;
                    if (url.contains(q)) {
                        prefix = link;
                    }
                }
                url += prefix + queryStr;
            }
        }
        return ofHttpRequestBuilder(url, headers, timeOut, gzip).build();
    }


    /**
     * 以json形式发送post请求
     * Content-Type:application/json;charset=utf-8
     *
     * @param url  请求地址
     * @param json json数据 可以为null
     */
    public static HttpResponse<String> postJson(String url, String json) {
        return post(url, null, json, null, null, -1, true, compressedBodyHandler);
    }

    /**
     * 以json形式发送post请求
     * Content-Type:application/json;charset=utf-8
     *
     * @param url  请求地址
     * @param json json数据 可以为null
     */
    public static HttpResponse<String> postJson(String url, String json,Map<String ,String> headers) {
        return post(url, null, json, null, headers, -1, true, compressedBodyHandler);
    }

    /**
     * 以json形式发送post异步请求
     * Content-Type:application/json;charset=utf-8
     *
     * @param url  请求地址
     * @param json json数据 可以为null
     */
    public static CompletableFuture<HttpResponse<String>> postJsonAsync(String url, String json) {
        return postAsync(url, null, json, null, null, -1, true, compressedBodyHandler);
    }

    /**
     * 以普通表单提交的方式发送post请求
     * Content-Type: application/x-www-form-urlencoded;charset=utf-8
     *
     * @param url     请求地址
     * @param formMap map参数
     */
    public static HttpResponse<String> postFormData(String url, Map<String, Object> formMap) {
        return post(url, formMap, null, null, null, -1, true, compressedBodyHandler);
    }
    /**
     * 以普通表单提交的方式发送post请求
     * Content-Type: application/x-www-form-urlencoded;charset=utf-8
     *
     * @param url     请求地址
     * @param formMap map参数
     */
    public static HttpResponse<String> postFormData(String url, Map<String, Object> formMap,Map<String,String> headers) {
        return post(url, formMap, null, null, headers, -1, true, compressedBodyHandler);
    }

    /**
     * 以普通表单提交的方式发送post异步请求
     * Content-Type: application/x-www-form-urlencoded;charset=utf-8
     *
     * @param url     请求地址
     * @param formMap map参数
     */
    public static CompletableFuture<HttpResponse<String>> postFormDataAsync(String url, Map<String, Object> formMap) {
        return postAsync(url, formMap, null, null, null, -1, true, compressedBodyHandler);
    }

    /**
     * multipart/form-data方式提交表单
     *
     * @param url 请求地址
     * @param map map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     */
    public static HttpResponse<String> postMultipart(String url, Map<String, Object> map) {
        return postMultipart(url, map, -1);
    }

    /**
     * multipart/form-data方式提交表单
     *
     * @param url     请求地址
     * @param map     map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     * @param timeOut 超时时间 秒
     */
    public static HttpResponse<String> postMultipart(String url, Map<String, Object> map, int timeOut) {
        return post(url, null, null, map, null, timeOut, true, compressedBodyHandler);
    }

    /**
     * multipart/form-data方式异步提交表单
     *
     * @param url     请求地址
     * @param map     map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     * @param timeOut 超时时间 秒
     */
    public static CompletableFuture<HttpResponse<String>> postMultipartAsync(String url, Map<String, Object> map, int timeOut) {
        return postAsync(url, null, null, map, null, timeOut, true, compressedBodyHandler);
    }

    /**
     * 发送post请求
     *
     * @param url                 请求地址
     * @param formDataMap         提交form表单数据时设置
     * @param json                发送json数据时设置
     * @param multipartMap        上传类型的表单数据  map的key为字段名 若是文件 map的value为Path类型 若为普通字段 value可以是基本类型
     * @param headers             请求头map
     * @param timeOut             超时时间 秒
     * @param gzip                启用gzip
     * @param responseBodyHandler responseBodyHandler
     */
    public static <T> HttpResponse<T>
    post(String url, Map<String, Object> formDataMap, String json, Map<String, Object> multipartMap, Map<String, String> headers,
         int timeOut, boolean gzip, HttpResponse.BodyHandler<T> responseBodyHandler) {
        HttpRequest request = ofPostHttpRequest(url, formDataMap, json, multipartMap, headers, timeOut, gzip);
        HttpResponse<T> response = null;
        try {
                HttpResponse<T> item = DEFAULT_HTTP_CLIENT.send(request, responseBodyHandler);
                if (item == null || item.statusCode() != HttpStatus.OK.value()) {
                    log.error("请求错误:" + item);
                    throw new HttpServerErrorException(HttpStatus.BAD_REQUEST);
                }
                return item;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 发送post异步请求
     *
     * @param url                 请求地址
     * @param formDataMap         提交form表单数据时设置
     * @param json                发送json数据时设置
     * @param multipartMap        上传类型的表单数据  map的key为字段名 若是文件 map的value为Path类型 若为普通字段 value可以是基本类型
     * @param headers             请求头map
     * @param timeOut             超时时间 秒
     * @param gzip                启用gzip
     * @param responseBodyHandler responseBodyHandler
     */
    public static <T> CompletableFuture<HttpResponse<T>>
    postAsync(String url, Map<String, Object> formDataMap, String json, Map<String, Object> multipartMap, Map<String, String> headers,
              int timeOut, boolean gzip, HttpResponse.BodyHandler<T> responseBodyHandler) {
        HttpRequest request = ofPostHttpRequest(url, formDataMap, json, multipartMap, headers, timeOut, gzip);
        return DEFAULT_HTTP_CLIENT.sendAsync(request, responseBodyHandler);
    }


    private static HttpRequest ofPostHttpRequest(String url, Map<String, Object> formDataMap, String json, Map<String, Object> multipartMap,
                                                 Map<String, String> headers, int timeOut, boolean gzip) {
        boolean formDataMapNotNull = formDataMap != null && !formDataMap.isEmpty();
        boolean jsonNotNull = json != null && !json.isBlank();
        boolean multipartMapNotNull = multipartMap != null && !multipartMap.isEmpty();

        if ((formDataMapNotNull ? 1 : 0) + (jsonNotNull ? 1 : 0) + (multipartMapNotNull ? 1 : 0) > 1) {
            throw new RuntimeException("发送post请求时,无法判断要发送哪种请求类型!");
        }

        String contentTypeValue = null;
        if (headers != null) {
            List<String> contentList = headers.keySet().stream().filter(k -> CONTENT_TYPE.equalsIgnoreCase(k.strip())).collect(Collectors.toList());
            if (contentList.size() > 1) {
                throw new RuntimeException("请求头内不能传递多个ContentType!");
            }
            if (!contentList.isEmpty()) {
                String k = contentList.get(0);
                contentTypeValue = headers.get(k);
                headers.remove(k);
            }
        }

        contentTypeValue = contentTypeValue != null && !contentTypeValue.isBlank() ? contentTypeValue : jsonNotNull ? "application/json; charset=UTF-8"
                : formDataMapNotNull ? "application/x-www-form-urlencoded; charset=UTF-8" : null;
        HttpRequest.BodyPublisher bodyPublisher = !(jsonNotNull || formDataMapNotNull) ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(jsonNotNull ? json : mapToQueryString(formDataMap, true));

        if (multipartMapNotNull) {
            String boundary = BOUNDARY_PREFIX + UUID.randomUUID().toString().replace("-", "");
            contentTypeValue = "multipart/form-data; boundary=" + boundary;
            bodyPublisher = ofMimeMultipartBodyPublisher(multipartMap, boundary);
        }

        HttpRequest.Builder builder = ofHttpRequestBuilder(url, headers, timeOut, gzip);
        if (contentTypeValue != null && !contentTypeValue.isBlank()) {
            builder.setHeader(CONTENT_TYPE, contentTypeValue.strip());
        }
        return builder.POST(bodyPublisher).build();
    }

    /**
     * webSocket
     *
     * @param url      url地址
     * @param headers  打开握手时发送的额外请求header(例如服务器设置了webSocket的路径访问也需要用户已登陆,这里可传递用户token),
     *                 注意不能传递<a href="https://tools.ietf.org/html/rfc6455#section-11.3">WebSocket协议</a>中已定义的header
     * @param listener WebSocket的接收接口
     */
    public static CompletableFuture<WebSocket> webSocket(String url, Map<String, String> headers, WebSocket.Listener listener) {
        WebSocket.Builder builder = DEFAULT_HTTP_CLIENT.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECOND));
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return builder.buildAsync(URI.create(url), listener);
    }


    /**
     * 获取HttpRequest.Builder
     *
     * @param url     请求地址
     * @param headers 请求头map
     * @param timeOut 超时时间,秒
     * @param gzip    启用gzip
     */
    private static HttpRequest.Builder ofHttpRequestBuilder(String url, Map<String, String> headers, int timeOut, boolean gzip) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
        if (gzip) {
            String acceptEncodingValue = null;
            if (headers != null) {
                List<String> contentList = headers.keySet().stream()
                        .filter(k -> ACCEPT_ENCODING.equalsIgnoreCase(k.strip()))
                        .collect(Collectors.toList());
                if (contentList.size() > 1) {
                    throw new RuntimeException("请求头内不能传递多个AcceptEncoding!");
                }
                if (!contentList.isEmpty()) {
                    String k = contentList.get(0);
                    acceptEncodingValue = headers.get(k);
                    headers.remove(k);
                }
            }
            acceptEncodingValue = acceptEncodingValue != null && !acceptEncodingValue.isBlank() ? acceptEncodingValue : GZIP;
            builder.setHeader(ACCEPT_ENCODING, acceptEncodingValue.strip());
        }

        if (headers != null) {
            headers.forEach(builder::setHeader);
        }
        builder.timeout(Duration.ofSeconds(timeOut > 0 ? timeOut : REQUEST_TIMEOUT_SECOND));
        return builder;
    }


    /**
     * 参数map转请求字符串
     * 若map为null返回 空字符串""
     *
     * @param map       参数map
     * @param urlEncode 是否进行UrlEncode编码(UTF-8)
     */
    private static String mapToQueryString(Map<String, Object> map, boolean urlEncode) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            if (sb.length() > 0) {
                sb.append("&");
            }
            String name = k.strip();
            String value = String.valueOf(v).strip();
            if (urlEncode) {
                value = URLEncoder.encode(value, StandardCharsets.UTF_8);
            }
            sb.append(name).append("=").append(value);
        });
        return sb.toString();
    }

    /**
     * 根据map boundary 构造mimeMultipartBodyPublisher
     *
     * @param map      map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     * @param boundary 边界
     */
    private static HttpRequest.BodyPublisher ofMimeMultipartBodyPublisher(Map<String, Object> map, String boundary) {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);
        map.forEach((k, v) -> {
            byteArrays.add(separator);
            if (v instanceof Path) {
                Path path = (Path) v;
                String mimeType;
                try {
                    mimeType = Files.probeContentType(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                mimeType = mimeType == null || mimeType.isBlank() ? "application/octet-stream" : mimeType;

                byteArrays.add(("\"" + k + "\"; filename=\"" + path.getFileName()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                try {
                    byteArrays.add(Files.readAllBytes(path));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + k + "\"\r\n\r\n" + v + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
        });
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    /*
     * 静态成员变量
     */
    private static final long CONNECT_TIMEOUT_SECOND = 5;
    private static final long REQUEST_TIMEOUT_SECOND = 10;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP = "gzip";
    private static final String BOUNDARY_PREFIX = "----JavaHttpClientBoundary";



    // 默认配置满足使用. 具体可查看源码，例如线程池(用于异步处理)，连接池等
    private static final java.net.http.HttpClient DEFAULT_HTTP_CLIENT = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECOND))
            // 版本 默认http2,不支持会自动降级
            .version(java.net.http.HttpClient.Version.HTTP_1_1)
            .executor(ThreadPoolExecutorUtil.getPoll("AsyncTaskExecutePool"))
            // 设置支持不安全的https
            //　.sslContext(ofUnsafeSslContext())
            // 重定向
            // .followRedirects(HttpClient.Redirect.NEVER)
            // .cookieHandler(CookieHandler.getDefault())
            // 代理
            // .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
            // 验证
            // .authenticator(Authenticator.getDefault())
            .build();

    /*
     * 关于错误的https证书:
     * 注意:在正常部署中，不希望使用下列机制中的任何一种，因为正常情况下应该可以自动验证任何正确配置的HTTPS服务器提供的证书
     *
     * 对于https证书错误,但是又想httpClient忽略证书错误正常执行,可以有下面几种解决办法:
     *   1. 构建一个SSLContext来忽略错误的证书,并且在初始化HttpClient客户端的时候传递进去.
     *       这样的问题在于,对于所有网址完全禁用了服务器身份验证
     *   2. 若不想采用上述办法,并且只有错误的证书比较少,比如一个,则可以使用以下命令将其导入密钥库
     *          keytool -importcert -keystore keystorename -storepass pass -alias cert -file certfile
     *      然后使用InputStream初始化SSLContext，如下所示读取密钥库：
     *          char[] passphrase = ..
     *          KeyStore ks = KeyStore.getInstance("PKCS12");
     *          ks.load(i, passphrase); // i is an InputStream reading the keystore
     *
     *          KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
     *          kmf.init(ks, passphrase);
     *
     *          TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
     *          tmf.init(ks);
     *
     *          sslContext = SSLContext.getInstance("TLS");
     *          sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
     *  3.  以上两种解决方案均适用于自签名证书。
     *      第三种选择是服务器提供有效的，非自签名的证书.但对于与它提供的证书中的任何名称都不匹配的host，
     *      则使用系统属性“ jdk.internal.httpclient.disableHostnameVerification”设置为“ true”，
     *      这将强制以以前使用HostnameVerifier API的相同方式来接受证书
     * */

    /**
     * 创建不安全的SSLContext,这将对于所有网址完全禁用了服务器身份验证
     */
    private static SSLContext ofUnsafeSslContext() {
        /*
         * jdk.internal.httpclient.disableHostnameVerification 是用来控制是否禁用主机名验证的
         * 查看源码可知
         *  1.AbstractAsyncSSLConnection的静态成员变量disableHostnameVerification类加载的时候从
         *      Util类的isHostnameVerificationDisabled()方法,而此方法是在Util类加载的时候从系统变量
         *      jdk.internal.httpclient.disableHostnameVerification读取而来
         *  2.AbstractAsyncSSLConnection的构造方法中调用了本类的createSSLParameters方法
         *      在此方法中,先从我们构建的httpClient中取出SSLParameters拷贝一份,若disableHostnameVerification为false
         *      则sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
         *
         * 注意:测试环境下使用自己造的证书,若主机名和证书不一样,需要配置此参数
         */
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        TrustManager[] trustAllCertificates = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
            }
        }};
        SSLContext unsafeSslContext;
        try {
            unsafeSslContext = SSLContext.getInstance("TLS");
            unsafeSslContext.init(null, trustAllCertificates, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("构造unsafeSslContext出现异常", e);
        }
        return unsafeSslContext;
    }

    /**
     * 处理压缩的BodyHandler
     * <br/><br/>
     * 注意:即使本httpClient请求头携带了Accept-Encoding: gzip头信息,服务器也可能不返回Content-Encoding: gzip头信息
     * <br/>
     * 这是因为:
     * <br/>
     * 1.服务器不支持或者没有开启gzip
     * <br/>
     * 2.有些服务器对本httpClient发送的请求就不响应Content-Encoding,但对浏览器却响应Content-Encoding
     * <br/>
     * 因此如果要测试gzip 建议访问github.com,经测试此网址用本httpClient访问可以返回Content-Encoding: gzip
     */
    private static final HttpResponse.BodyHandler<String> compressedBodyHandler = responseInfo -> {
        Map<String, List<String>> headersMap = responseInfo.headers().map();
        List<String> gzipList = new ArrayList<>();
        AtomicReference<String> typeAtomic = new AtomicReference<>();
        headersMap.forEach((k, values) -> {
            String s = k.strip();
            if (CONTENT_ENCODING.equalsIgnoreCase(s)) {
                gzipList.addAll(values.stream().map(String::toLowerCase).collect(Collectors.toList()));
            }
            if (CONTENT_TYPE.equalsIgnoreCase(s)) {
                typeAtomic.set(values.stream().findFirst().orElse("text/html; charset=utf-8"));
            }
        });
        // 参考自HttpResponse.BodyHandlers.ofString()
        String type = typeAtomic.get();
        Charset charset = null;
        try {
            int i = type.indexOf(";");
            if (i >= 0) {
                type = type.substring(i + 1);
            }
            int index = type.indexOf("=");
            if (index >= 0 && type.toLowerCase().contains("charset")) {
                charset = Charset.forName(type.substring(index + 1));
            }
        } catch (Throwable x) {
            x.printStackTrace();
        }
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        if (gzipList.contains(GZIP)) {
            Charset finalCharset = charset;
            /*
             * 此处存在一个java11的bug,在java13中已修复
             * 参考链接 https://stackoverflow.com/questions/64447837/how-to-consume-an-inputstream-in-an-httpresponse-bodyhandler
             * 在java11环境下,若在此使用ofInputStream(),永远不会从流中读取任何数据，并且永远挂起
             * 因此这里暂时使用HttpResponse.BodySubscribers.ofByteArray()
             * */
            return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofByteArray(),
                    byteArray -> {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                             InputStream is = new GZIPInputStream(new ByteArrayInputStream(byteArray))) {
                            is.transferTo(os);
                            return new String(os.toByteArray(), finalCharset);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        return HttpResponse.BodySubscribers.ofString(charset);
    };

}