package com.mediajourney.videocache;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mediajourney.videocache.Preconditions.checkNotNull;

/**
 * Model for Http GET request.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class GetRequest {

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    private static final Pattern URL_PATTERN = Pattern.compile("GET /(.*) HTTP");

    public final String uri;
    public final long rangeOffset;
    public final boolean partial;

    /**
     * 通过正则表达式 匹配到range的offset和url
     * @param request
     */
    public GetRequest(String request) {
        checkNotNull(request);
        //是否是分块请求，获取分块请求的开始偏移
        long offset = findRangeOffset(request);
        this.rangeOffset = Math.max(0, offset);
        this.partial = offset >= 0;
        //获取真实的url（去掉了本地代理的前缀，如果是ping的请求对应的是ping,否则就是真实的url）
        this.uri = findUri(request);
    }

    public static GetRequest read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder stringRequest = new StringBuilder();
        String line;
        while (!TextUtils.isEmpty(line = reader.readLine())) { // until new line (headers ending)
            stringRequest.append(line).append('\n');
        }
        //GET /ping HTTP/1.1
        //User-Agent: Dalvik/2.1.0 (Linux; U; Android 8.0.0; Android SDK built for x86 Build/OSR1.180418.026)
        //Host: 127.0.0.1:39766
        //Connection: Keep-Alive
        //Accept-Encoding: gzip
        return new GetRequest(stringRequest.toString());
    }

    private long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return -1;
    }

    private String findUri(String request) {
        Matcher matcher = URL_PATTERN.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid request `" + request + "`: url not found!");
    }

    @Override
    public String toString() {
        return "GetRequest{" +
                "rangeOffset=" + rangeOffset +
                ", partial=" + partial +
                ", uri='" + uri + '\'' +
                '}';
    }
}
