package com.jph.takephoto.compress;

import android.content.Context;
import android.text.TextUtils;

import com.jph.takephoto.model.LubanOptions;
import com.jph.takephoto.model.TImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;
import me.shaohui.advancedluban.OnMultiCompressListener;

/**
 * 压缩照片,采用luban
 * Author: crazycodeboy
 * Date: 2016/11/5 0007 20:10
 * Version:4.0.0
 * 技术博文：http://www.devio.org/
 * GitHub:https://github.com/crazycodeboy
 * Eamil:crazycodeboy@gmail.com
 */
public class CompressWithLuBan implements CompressImage {
    private ArrayList<TImage> images;
    private CompressListener listener;
    private Context context;
    private LubanOptions options;
    private ArrayList<File> files = new ArrayList<>();

    public CompressWithLuBan(Context context, CompressConfig config, ArrayList<TImage> images,
                             CompressListener listener) {
        options = config.getLubanOptions();
        this.images = images;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void compress() {
        if (images == null || images.isEmpty()) {
            listener.onCompressFailed(images, " images is null");
            return;
        }
        for (TImage image : images) {
            if (image == null) {
                listener.onCompressFailed(images, " There are pictures of compress  is null.");
                return;
            }
            if (CompressImageUtil.getFileSize(image.getOriginalPath()) > options.getMaxSize()) {
                files.add(new File(image.getOriginalPath()));
            } else {
                image.setCompressed(true);
                image.setCompressPath(image.getOriginalPath());
            }
        }
        if (images.size() == 1) {
            compressOne();
        } else {
            compressMulti();
        }
    }

    private void compressOne() {
        if (CompressImageUtil.getFileSize(images.get(0).getOriginalPath()) > options.getMaxSize()) {
            Luban.compress(context, files.get(0))
                    .putGear(Luban.CUSTOM_GEAR)
                    .setMaxHeight(options.getMaxHeight())
                    .setMaxWidth(options.getMaxWidth())
                    .setMaxSize(options.getMaxSize() / 1000)
                    .launch(new OnCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(File file) {
                            TImage image = images.get(0);
                            image.setCompressPath(file.getPath());
                            image.setCompressed(true);
                            listener.onCompressSuccess(images);
                        }

                        @Override
                        public void onError(Throwable e) {
                            listener.onCompressFailed(images, e.getMessage() + " is compress failures");
                        }
                    });
        } else {
            listener.onCompressSuccess(images);
        }
    }

    private void compressMulti() {
        if(files.size()>0) {
            Luban.compress(context, files)
                    .putGear(Luban.CUSTOM_GEAR)
                    .setMaxSize(
                            options.getMaxSize() / 1000)                // limit the final image size（unit：Kb）
                    .setMaxHeight(options.getMaxHeight())             // limit image height
                    .setMaxWidth(options.getMaxWidth())
                    .launch(new OnMultiCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(List<File> fileList) {
                            handleCompressCallBack(fileList);
                        }

                        @Override
                        public void onError(Throwable e) {
                            listener.onCompressFailed(images, e.getMessage() + " is compress failures");
                        }
                    });
        }else {
            listener.onCompressSuccess(images);
        }
    }


    private void handleCompressCallBack(List<File> files) {
        for (int i = 0, j = 0; i < images.size() && j < files.size(); i++) {
            TImage image = images.get(i);
            if (TextUtils.isEmpty(image.getCompressPath())) {
                image.setCompressed(true);
                image.setCompressPath(files.get(j++).getPath());
            }
        }
        listener.onCompressSuccess(images);
    }
}
