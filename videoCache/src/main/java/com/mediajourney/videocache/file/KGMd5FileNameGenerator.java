package com.mediajourney.videocache.file;


import android.text.TextUtils;

public class KGMd5FileNameGenerator extends Md5FileNameGenerator {

    @Override
    public String generate(String url) {
        String extension = getExtension(url);
        String name = getCacheName(url);
//        if(TextUtils.isEmpty(name)){
//            name = generate(url);
//        }
        return TextUtils.isEmpty(extension) ? name : name + "." + extension;
    }

    private String getCacheName(String url) {
        int dotIndex = url.lastIndexOf('.');
        int slashIndex = url.lastIndexOf('/');
        return dotIndex != -1 && dotIndex > slashIndex ? url.substring(slashIndex + 1, dotIndex): "";
    }
}
