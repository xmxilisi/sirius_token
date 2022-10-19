package com.contract.modules.contract.utils

import com.contract.config.thread.ThreadPoolExecutorUtil
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.ResponseInfo
import java.net.http.WebSocket
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiConsumer
import java.util.stream.Collectors
import java.util.zip.GZIPInputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/11
 * @Version: 1.0
 * @Description:
 */
@Slf4j
object HttpClient {
    var log: Logger = log(this);
    /**
     * 发送get请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    /**
     * 发送get请求
     *
     * @param url 请求地址，可以拼接参数
     */
    @JvmOverloads
    operator fun get(url: String, params: MutableMap<String, Any>? = null): HttpResponse<String>? {
        return get(url, params, null, -1, true, compressedBodyHandler)
    }

    /**
     * 发送get请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    operator fun get(
        url: String,
        params: MutableMap<String, Any>?,
        headers: MutableMap<String, String?>?
    ): HttpResponse<String>? {
        return get(url, params, headers, -1, true, compressedBodyHandler)
    }

    /**
     * 发送get异步请求
     *
     * @param url 请求地址，可以拼接参数
     */
    fun getAsync(url: String): CompletableFuture<HttpResponse<String>> {
        return getAsync(url, null)
    }

    /**
     * 发送get异步请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    fun getAsync(url: String, params: MutableMap<String, Any>?): CompletableFuture<HttpResponse<String>> {
        return getAsync(url, params, null, -1, true, compressedBodyHandler)
    }

    /**
     * 发送get异步请求
     *
     * @param url    请求地址，可以拼接参数
     * @param params 请求参数
     */
    fun getAsync(
        url: String,
        params: MutableMap<String, Any>?,
        headers: MutableMap<String, String?>?
    ): CompletableFuture<HttpResponse<String>> {
        return getAsync(url, params, headers, -1, true, compressedBodyHandler)
    }

    /**
     * 下载文件
     * <br></br>
     * 若确定服务器可以响应Content-Disposition: attachment; filename=a.xx
     * 那么fileName文件名可以不传递,否则必须传递fileName
     *
     * @param url       请求路径
     * @param directory 保存的文件目录
     * @param fileName  文件名称 可以不传递
     * @param timeOut   超时时间 秒
     */
    fun downLoad(url: String, directory: String, fileName: String?, timeOut: Int): HttpResponse<Path>? {
        val bodyHandler: BodyHandler<Path>
        if (!Files.isDirectory(Path.of(directory))) {
            throw RuntimeException("不是一个目录: $directory")
        }
        bodyHandler = if (fileName == null || fileName.isBlank()) {
            HttpResponse.BodyHandlers.ofFileDownload(
                Path.of(directory),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            )
        } else {
            HttpResponse.BodyHandlers.ofFile(Path.of(directory, fileName))
        }
        return get(url, null, null, timeOut, false, bodyHandler)
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
    operator fun <T> get(
        url: String,
        params: MutableMap<String, Any>?,
        headers: MutableMap<String, String?>?,
        timeOut: Int,
        gzip: Boolean,
        responseBodyHandler: BodyHandler<T>?
    ): HttpResponse<T>? {
        val request = ofGetHttpRequest(url, params, headers, timeOut, gzip)
        try {
            return DEFAULT_HTTP_CLIENT.send(request, responseBodyHandler)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return null
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
    fun <T> getAsync(
        url: String,
        params: MutableMap<String, Any>?,
        headers: MutableMap<String, String?>?,
        timeOut: Int,
        gzip: Boolean,
        responseBodyHandler: BodyHandler<T>?
    ): CompletableFuture<HttpResponse<T>> {
        val request = ofGetHttpRequest(url, params, headers, timeOut, gzip)
        return DEFAULT_HTTP_CLIENT.sendAsync(request, responseBodyHandler)
    }

    private fun ofGetHttpRequest(
        url: String, params: MutableMap<String, Any>?, headers: MutableMap<String, String?>?,
        timeOut: Int, gzip: Boolean
    ): HttpRequest {
        var url = url
        if (params != null && !params.isEmpty()) {
            val strip = url.strip()
            url = if (strip.endsWith("/")) strip.substring(0, strip.length - 1) else strip
            val q = "?"
            val link = "&"
            val i = url.indexOf(q)
            if (i != -1 && i != url.length - 1) {
                val paramsStr = url.substring(i + 1)
                for (s in paramsStr.split(link.toRegex()).toTypedArray()) {
                    val pair = s.split("=".toRegex()).toTypedArray()
                    if (pair.size == 2) {
                        params[pair[0]] = pair[1]
                    }
                }
                url = url.substring(0, i)
            }
            val queryStr = mapToQueryString(params, true)
            if (!queryStr.isEmpty()) {
                var prefix = ""
                if (!url.endsWith(q) && !url.endsWith(link)) {
                    prefix = q
                    if (url.contains(q)) {
                        prefix = link
                    }
                }
                url += prefix + queryStr
            }
        }
        return ofHttpRequestBuilder(url, headers, timeOut, gzip).build()
    }

    /**
     * 以json形式发送post请求
     * Content-Type:application/json;charset=utf-8
     *
     * @param url  请求地址
     * @param json json数据 可以为null
     */
    fun postJson(url: String, json: String?): HttpResponse<String>? {
        return post(url, null, json, null, null, -1, true, compressedBodyHandler)
    }

    /**
     * 以json形式发送post请求
     * Content-Type:application/json;charset=utf-8
     *
     * @param url  请求地址
     * @param json json数据 可以为null
     */
    fun postJson(url: String, json: String?, headers: MutableMap<String, String?>?): HttpResponse<String>? {
        return post(url, null, json, null, headers, -1, true, compressedBodyHandler)
    }

    /**
     * 以json形式发送post异步请求
     * Content-Type:application/json;charset=utf-8
     *
     * @param url  请求地址
     * @param json json数据 可以为null
     */
    fun postJsonAsync(url: String, json: String?): CompletableFuture<HttpResponse<String>> {
        return postAsync(url, null, json, null, null, -1, true, compressedBodyHandler)
    }

    /**
     * 以普通表单提交的方式发送post请求
     * Content-Type: application/x-www-form-urlencoded;charset=utf-8
     *
     * @param url     请求地址
     * @param formMap map参数
     */
    fun postFormData(url: String, formMap: Map<String, Any>?): HttpResponse<String>? {
        return post(url, formMap, null, null, null, -1, true, compressedBodyHandler)
    }

    /**
     * 以普通表单提交的方式发送post请求
     * Content-Type: application/x-www-form-urlencoded;charset=utf-8
     *
     * @param url     请求地址
     * @param formMap map参数
     */
    fun postFormData(
        url: String,
        formMap: Map<String, Any>?,
        headers: MutableMap<String, String?>?
    ): HttpResponse<String>? {
        return post(url, formMap, null, null, headers, -1, true, compressedBodyHandler)
    }

    /**
     * 以普通表单提交的方式发送post异步请求
     * Content-Type: application/x-www-form-urlencoded;charset=utf-8
     *
     * @param url     请求地址
     * @param formMap map参数
     */
    fun postFormDataAsync(url: String, formMap: Map<String, Any>?): CompletableFuture<HttpResponse<String>> {
        return postAsync(url, formMap, null, null, null, -1, true, compressedBodyHandler)
    }
    /**
     * multipart/form-data方式提交表单
     *
     * @param url     请求地址
     * @param map     map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     * @param timeOut 超时时间 秒
     */
    /**
     * multipart/form-data方式提交表单
     *
     * @param url 请求地址
     * @param map map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     */
    @JvmOverloads
    fun postMultipart(url: String, map: Map<String, Any>?, timeOut: Int = -1): HttpResponse<String>? {
        return post(url, null, null, map, null, timeOut, true, compressedBodyHandler)
    }

    /**
     * multipart/form-data方式异步提交表单
     *
     * @param url     请求地址
     * @param map     map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     * @param timeOut 超时时间 秒
     */
    fun postMultipartAsync(url: String, map: Map<String, Any>?, timeOut: Int): CompletableFuture<HttpResponse<String>> {
        return postAsync(url, null, null, map, null, timeOut, true, compressedBodyHandler)
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
    fun <T> post(
        url: String,
        formDataMap: Map<String, Any>?,
        json: String?,
        multipartMap: Map<String, Any>?,
        headers: MutableMap<String, String?>?,
        timeOut: Int,
        gzip: Boolean,
        responseBodyHandler: BodyHandler<T>?
    ): HttpResponse<T>? {
        val request = ofPostHttpRequest(url, formDataMap, json, multipartMap, headers, timeOut, gzip)
        val response: HttpResponse<T>? = null
        try {
            val item = DEFAULT_HTTP_CLIENT.send(request, responseBodyHandler)
            if (item == null || item.statusCode() != HttpStatus.OK.value()) {
                HttpClient.log.error("请求错误:$item")
                throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
            }
            return item
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
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
    fun <T> postAsync(
        url: String,
        formDataMap: Map<String, Any>?,
        json: String?,
        multipartMap: Map<String, Any>?,
        headers: MutableMap<String, String?>?,
        timeOut: Int,
        gzip: Boolean,
        responseBodyHandler: BodyHandler<T>?
    ): CompletableFuture<HttpResponse<T>> {
        val request = ofPostHttpRequest(url, formDataMap, json, multipartMap, headers, timeOut, gzip)
        return DEFAULT_HTTP_CLIENT.sendAsync(request, responseBodyHandler)
    }

    private fun ofPostHttpRequest(
        url: String, formDataMap: Map<String, Any>?, json: String?, multipartMap: Map<String, Any>?,
        headers: MutableMap<String, String?>?, timeOut: Int, gzip: Boolean
    ): HttpRequest {
        val formDataMapNotNull = formDataMap != null && !formDataMap.isEmpty()
        val jsonNotNull = json != null && !json.isBlank()
        val multipartMapNotNull = multipartMap != null && !multipartMap.isEmpty()
        if ((if (formDataMapNotNull) 1 else 0) + (if (jsonNotNull) 1 else 0) + (if (multipartMapNotNull) 1 else 0) > 1) {
            throw RuntimeException("发送post请求时,无法判断要发送哪种请求类型!")
        }
        var contentTypeValue: String? = null
        if (headers != null) {
            val contentList = headers.keys.stream().filter { k: String ->
                CONTENT_TYPE.equals(
                    k.strip(),
                    ignoreCase = true
                )
            }.collect(Collectors.toList())
            if (contentList.size > 1) {
                throw RuntimeException("请求头内不能传递多个ContentType!")
            }
            if (!contentList.isEmpty()) {
                val k = contentList[0]
                contentTypeValue = headers[k]
                headers.remove(k)
            }
        }
        contentTypeValue =
            if (contentTypeValue != null && !contentTypeValue.isBlank()) contentTypeValue else if (jsonNotNull) "application/json; charset=UTF-8" else if (formDataMapNotNull) "application/x-www-form-urlencoded; charset=UTF-8" else null
        var bodyPublisher =
            if (!(jsonNotNull || formDataMapNotNull)) HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(
                if (jsonNotNull) json else mapToQueryString(formDataMap, true)
            )
        if (multipartMapNotNull) {
            val boundary = BOUNDARY_PREFIX + UUID.randomUUID().toString().replace("-", "")
            contentTypeValue = "multipart/form-data; boundary=$boundary"
            bodyPublisher = ofMimeMultipartBodyPublisher(multipartMap, boundary)
        }
        val builder = ofHttpRequestBuilder(url, headers, timeOut, gzip)
        if (contentTypeValue != null && !contentTypeValue.isBlank()) {
            builder.setHeader(CONTENT_TYPE, contentTypeValue.strip())
        }
        return builder.POST(bodyPublisher).build()
    }

    /**
     * webSocket
     *
     * @param url      url地址
     * @param headers  打开握手时发送的额外请求header(例如服务器设置了webSocket的路径访问也需要用户已登陆,这里可传递用户token),
     * 注意不能传递[WebSocket协议](https://tools.ietf.org/html/rfc6455#section-11.3)中已定义的header
     * @param listener WebSocket的接收接口
     */
    fun webSocket(
        url: String?,
        headers: Map<String?, String?>?,
        listener: WebSocket.Listener?
    ): CompletableFuture<WebSocket> {
        val builder = DEFAULT_HTTP_CLIENT.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECOND))
        headers?.forEach { (name: String?, value: String?) ->
            builder.header(
                name,
                value
            )
        }
        return builder.buildAsync(URI.create(url), listener)
    }

    /**
     * 获取HttpRequest.Builder
     *
     * @param url     请求地址
     * @param headers 请求头map
     * @param timeOut 超时时间,秒
     * @param gzip    启用gzip
     */
    private fun ofHttpRequestBuilder(
        url: String,
        headers: MutableMap<String, String?>?,
        timeOut: Int,
        gzip: Boolean
    ): HttpRequest.Builder {
        val builder = HttpRequest.newBuilder(URI.create(url))
        if (gzip) {
            var acceptEncodingValue: String? = null
            if (headers != null) {
                val contentList = headers.keys.stream()
                    .filter { k: String ->
                        ACCEPT_ENCODING.equals(
                            k.strip(),
                            ignoreCase = true
                        )
                    }
                    .collect(Collectors.toList())
                if (contentList.size > 1) {
                    throw RuntimeException("请求头内不能传递多个AcceptEncoding!")
                }
                if (!contentList.isEmpty()) {
                    val k = contentList[0]
                    acceptEncodingValue = headers[k]
                    headers.remove(k)
                }
            }
            acceptEncodingValue =
                if (acceptEncodingValue != null && !acceptEncodingValue.isBlank()) acceptEncodingValue else GZIP
            builder.setHeader(ACCEPT_ENCODING, acceptEncodingValue.strip())
        }
        headers?.forEach { (name: String?, value: String?) ->
            builder.setHeader(
                name,
                value
            )
        }
        builder.timeout(Duration.ofSeconds((if (timeOut > 0) timeOut else REQUEST_TIMEOUT_SECOND) as Long))
        return builder
    }

    /**
     * 参数map转请求字符串
     * 若map为null返回 空字符串""
     *
     * @param map       参数map
     * @param urlEncode 是否进行UrlEncode编码(UTF-8)
     */
    private fun mapToQueryString(map: Map<String, Any>?, urlEncode: Boolean): String {
        if (map == null || map.isEmpty()) {
            return ""
        }
        val sb = StringBuilder()
        map.forEach(BiConsumer { k: String, v: Any ->
            if (sb.length > 0) {
                sb.append("&")
            }
            val name = k.strip()
            var value: String? = null
            value = if (v.javaClass.isArray) {
                java.lang.String.join(",", *v as Array<String?>)
            } else {
                v.toString().strip()
            }
            if (urlEncode) {
                value = URLEncoder.encode(value, StandardCharsets.UTF_8)
            }
            sb.append(name).append("=").append(value)
        })
        return sb.toString()
    }

    /**
     * 根据map boundary 构造mimeMultipartBodyPublisher
     *
     * @param map      map的key为字段名; value:若是文件为Path类型,若为普通字段是基本类型
     * @param boundary 边界
     */
    private fun ofMimeMultipartBodyPublisher(map: Map<String, Any>?, boundary: String): BodyPublisher {
        val byteArrays = ArrayList<ByteArray>()
        val separator = "--$boundary\r\nContent-Disposition: form-data; name=".toByteArray(StandardCharsets.UTF_8)
        map!!.forEach { (k: String, v: Any) ->
            byteArrays.add(separator)
            if (v is Path) {
                val path = v
                var mimeType: String?
                mimeType = try {
                    Files.probeContentType(path)
                } catch (e: IOException) {
                    throw UncheckedIOException(e)
                }
                mimeType = if (mimeType == null || mimeType.isBlank()) "application/octet-stream" else mimeType
                byteArrays.add(
                    ("\"" + k + "\"; filename=\"" + path.fileName
                            + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").toByteArray(StandardCharsets.UTF_8)
                )
                try {
                    byteArrays.add(Files.readAllBytes(path))
                } catch (e: IOException) {
                    throw UncheckedIOException(e)
                }
                byteArrays.add("\r\n".toByteArray(StandardCharsets.UTF_8))
            } else {
                byteArrays.add(
                    """"$k"
    
    $v
    """.toByteArray(StandardCharsets.UTF_8)
                )
            }
        }
        byteArrays.add("--$boundary--".toByteArray(StandardCharsets.UTF_8))
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays)
    }

    /*
     * 静态成员变量
     */
    private const val CONNECT_TIMEOUT_SECOND: Long = 5
    private const val REQUEST_TIMEOUT_SECOND: Long = 10
    private const val CONTENT_TYPE = "Content-Type"
    private const val ACCEPT_ENCODING = "Accept-Encoding"
    private const val CONTENT_ENCODING = "Content-Encoding"
    private const val GZIP = "gzip"
    private const val BOUNDARY_PREFIX = "----JavaHttpClientBoundary"

    // 默认配置满足使用. 具体可查看源码，例如线程池(用于异步处理)，连接池等
    private val DEFAULT_HTTP_CLIENT = java.net.http.HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECOND)) // 版本 默认http2,不支持会自动降级
        .version(java.net.http.HttpClient.Version.HTTP_1_1)
        .executor(ThreadPoolExecutorUtil.getPoll("sc-quartz-job")) // 设置支持不安全的https
        //　.sslContext(ofUnsafeSslContext())
        // 重定向
        // .followRedirects(HttpClient.Redirect.NEVER)
        // .cookieHandler(CookieHandler.getDefault())
        // 代理
        // .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
        // 验证
        // .authenticator(Authenticator.getDefault())
        .build()
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
    private fun ofUnsafeSslContext(): SSLContext {
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
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true")
        val trustAllCertificates = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkClientTrusted(arg0: Array<X509Certificate>, arg1: String) {}
            override fun checkServerTrusted(arg0: Array<X509Certificate>, arg1: String) {}
        })
        val unsafeSslContext: SSLContext
        try {
            unsafeSslContext = SSLContext.getInstance("TLS")
            unsafeSslContext.init(null, trustAllCertificates, SecureRandom())
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("构造unsafeSslContext出现异常", e)
        } catch (e: KeyManagementException) {
            throw RuntimeException("构造unsafeSslContext出现异常", e)
        }
        return unsafeSslContext
    }

    /**
     * 处理压缩的BodyHandler
     * <br></br><br></br>
     * 注意:即使本httpClient请求头携带了Accept-Encoding: gzip头信息,服务器也可能不返回Content-Encoding: gzip头信息
     * <br></br>
     * 这是因为:
     * <br></br>
     * 1.服务器不支持或者没有开启gzip
     * <br></br>
     * 2.有些服务器对本httpClient发送的请求就不响应Content-Encoding,但对浏览器却响应Content-Encoding
     * <br></br>
     * 因此如果要测试gzip 建议访问github.com,经测试此网址用本httpClient访问可以返回Content-Encoding: gzip
     */
    private val compressedBodyHandler = label@ BodyHandler { responseInfo: ResponseInfo ->
        val headersMap =
            responseInfo.headers().map()
        val gzipList: MutableList<String> = ArrayList()
        val typeAtomic = AtomicReference<String>()
        headersMap.forEach { (k: String, values: List<String>) ->
            val s = k.strip()
            if (CONTENT_ENCODING.equals(s, ignoreCase = true)) {
                gzipList.addAll(
                    values.stream()
                        .map { obj: String ->
                            obj.lowercase(
                                Locale.getDefault()
                            )
                        }
                        .collect(Collectors.toList())
                )
            }
            if (CONTENT_TYPE.equals(s, ignoreCase = true)) {
                typeAtomic.set(values.stream().findFirst().orElse("text/html; charset=utf-8"))
            }
        }
        // 参考自HttpResponse.BodyHandlers.ofString()
        var type = typeAtomic.get()
        var charset: Charset? = null
        try {
            val i = type.indexOf(";")
            if (i >= 0) {
                type = type.substring(i + 1)
            }
            val index = type.indexOf("=")
            if (index >= 0 && type.lowercase(Locale.getDefault()).contains("charset")) {
                charset = Charset.forName(type.substring(index + 1))
            }
        } catch (x: Throwable) {
            x.printStackTrace()
        }
        charset = charset ?: StandardCharsets.UTF_8
        if (gzipList.contains(GZIP)) {
            val finalCharset = charset
            /*
              * 此处存在一个java11的bug,在java13中已修复
              * 参考链接 https://stackoverflow.com/questions/64447837/how-to-consume-an-inputstream-in-an-httpresponse-bodyhandler
              * 在java11环境下,若在此使用ofInputStream(),永远不会从流中读取任何数据，并且永远挂起
              * 因此这里暂时使用HttpResponse.BodySubscribers.ofByteArray()
            * */
            return@BodyHandler HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofByteArray()
            ) { byteArray: ByteArray? ->
                try {
                    ByteArrayOutputStream().use { os ->
                        GZIPInputStream(ByteArrayInputStream(byteArray)).use { `is` ->
                            `is`.transferTo(os)
                            return@mapping String(os.toByteArray(), (finalCharset)!!)
                        }
                    }
                } catch (e: IOException) {
                    throw UncheckedIOException(e)
                }
            }
        }
        HttpResponse.BodySubscribers.ofString(charset)
    }
}