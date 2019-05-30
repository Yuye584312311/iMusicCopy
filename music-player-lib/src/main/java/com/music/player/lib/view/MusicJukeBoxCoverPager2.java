package com.music.player.lib.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import com.music.player.lib.R;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.model.MusicGlideCircleTransform;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicImageCache;
import com.music.player.lib.util.MusicUtils;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/6
 * MusicJukeBoxCoverPager
 * @2019-05-30: 对唱片封面模块优化，使用普通的图片加载模式，避免内存占用高等问题，其功能MusicJukeBoxView保持一致
 */

public class MusicJukeBoxCoverPager2 extends LinearLayout {

    private static final String TAG = "MusicJukeBoxCoverPager2";
    private Context mContext;
    //唱片机旋转一圈耗时
    private int mRotationDurtion;
    private ObjectAnimator mDiscObjectAnimator;
    private ImageView mDiseCover;
    private int mJukeBoxCoverFgSize;

    public MusicJukeBoxCoverPager2(Context context) {
        this(context,null);
    }

    public MusicJukeBoxCoverPager2(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicJukeBoxCoverPager2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.music_view_cover_pager2,this);
        this.mContext=context;
        FrameLayout coverLayout = (FrameLayout) findViewById(R.id.cover_frame_layout);
        //胶片背景
        ImageView discBg = (ImageView) findViewById(R.id.view_dise_bg);
        //胶片封面
        mDiseCover = (ImageView) findViewById(R.id.view_dise_cover);
        int screenWidth = MusicUtils.getInstance().getScreenWidth(context);
        //背景图片大小
        int jukeBoxCoverBgSize = (int) (screenWidth * MusicConstants.SCALE_DISC_SIZE);
        //封面大小
        mJukeBoxCoverFgSize = (int) (screenWidth * MusicConstants.SCALE_MUSIC_PIC_SIZE);
        //背景距离顶部高度
        int marginTop = (int) (MusicConstants.SCALE_DISC_MARGIN_TOP * screenWidth);

        LinearLayout.LayoutParams layoutParams = (LayoutParams) coverLayout.getLayoutParams();
        layoutParams.setMargins(0,marginTop,0,0);
        layoutParams.width=jukeBoxCoverBgSize;
        layoutParams.height=jukeBoxCoverBgSize;
        coverLayout.setLayoutParams(layoutParams);

        //确定背景圆盘大小及位置
        FrameLayout.LayoutParams bgLayoutParams = (FrameLayout.LayoutParams) discBg.getLayoutParams();
        bgLayoutParams.width=jukeBoxCoverBgSize;
        bgLayoutParams.height=jukeBoxCoverBgSize;
        discBg.setLayoutParams(bgLayoutParams);

        //确定其大小和位置,正好位于父容器的中央背景圆盘位置
        FrameLayout.LayoutParams coverLayoutParams = (FrameLayout.LayoutParams) mDiseCover.getLayoutParams();
        coverLayoutParams.width= mJukeBoxCoverFgSize;
        coverLayoutParams.height= mJukeBoxCoverFgSize;
        mDiseCover.setLayoutParams(coverLayoutParams);

        discBg.setImageResource(R.drawable.ic_music_disc);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicJukeBoxCoverPager);
            mRotationDurtion = typedArray.getInteger(R.styleable.MusicJukeBoxCoverPager_musicJukeRotationDurtion,
                    MusicConstants.BOX_REVOLVE_MINUTE);
            typedArray.recycle();
        }else{
            mRotationDurtion=MusicConstants.BOX_REVOLVE_MINUTE;
        }
    }

    /**
     * 设置封面
     * @param drawable 封面位图
     */
    public void setMusicCover(Drawable drawable){
        if(null!=drawable&&null!=mDiseCover){
            BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;
            setMusicCover(bitmapDrawable.getBitmap());
        }
    }

    /**
     * 设置封面
     * @param bitmap 封面位图
     */
    public void setMusicCover(Bitmap bitmap){
        if(null!=bitmap&&null!=mDiseCover){
            Bitmap resultBitmap = MusicUtils.getInstance().drawRoundBitmap(bitmap);
            mDiseCover.setImageBitmap(resultBitmap);
            bitmap.recycle();
            resultBitmap.recycle();
        }
    }

    /**
     * 设置封面
     * @param filePath http 或者 file://
     */
    public void setMusicCover(String filePath){
        if(null==mDiseCover) return;
        //HTTP || HTTPS
        if(filePath.startsWith("http:")|| filePath.startsWith("https:")){
            Logger.d(TAG,"setMusicComposeFront-->HTTP || HTTPS");
            Glide.with(getContext())
                    .load(filePath)
                    .asBitmap()
                    .error(R.drawable.ic_music_juke_default_cover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .transform(new MusicGlideCircleTransform(getContext()))
                    .into(mDiseCover);
        }else{
            Logger.d(TAG,"setMusicCover-->File");
            //File
            Bitmap bitmap;
            bitmap = MusicImageCache.getInstance().getBitmap(filePath);
            //缓存为空，获取音频文件自身封面
            if(null==bitmap){
                bitmap=MusicImageCache.getInstance().createBitmap(filePath);
            }
            if(null!=bitmap){
                setMusicCover(bitmap);
            }else{
                mDiseCover.setImageResource(R.drawable.ic_music_juke_default_cover);
            }
        }
    }

    /**
     * 设置唱片机旋转一圈耗时
     * @param rotationDurtion
     */
    public void setRotationDurtion(int rotationDurtion) {
        this.mRotationDurtion = rotationDurtion;
    }

    /**
     * 创建一个旋转动画实体
     * @return
     */
    private ObjectAnimator getDiscObjectAnimator() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mDiseCover, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(mRotationDurtion * 1000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        this.mDiscObjectAnimator=objectAnimator;
        return objectAnimator;
    }

    /**
     * 控制器透明度
     * @param alpha
     */
    public void setConntrollerAlpha(float alpha) {}

    public ObjectAnimator getObjectAnimator() {
        if(null==mDiscObjectAnimator){
            return getDiscObjectAnimator();
        }
        return mDiscObjectAnimator;
    }

    public void onStart() {
        if(null==mDiscObjectAnimator){
            mDiscObjectAnimator = getDiscObjectAnimator();
        }
        mDiscObjectAnimator.start();
    }

    public void onStop() {
        if(null!=mDiscObjectAnimator){
            mDiscObjectAnimator.cancel();
            mDiscObjectAnimator=null;
        }
        if(null!=mDiseCover) mDiseCover.setRotation(0);
    }

    public void onReset() {
        onStop();
    }

    /**
     * 回收ImageView的Bitmap
     * @param imageView imageView
     */
    private void recyclerImageViewBitmap(ImageView imageView) {
        if(null!=imageView&&null!=imageView.getDrawable()){
            try {
                Drawable drawable = imageView.getDrawable();
                if(drawable instanceof BitmapDrawable){
                    BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    if(!bitmap.isRecycled()){
                        Logger.d(TAG,"recyclerImageViewBitmap-->");
                        bitmap.recycle();
                    }
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    public void onDestroy(){
        recyclerImageViewBitmap(mDiseCover);
        mContext=null;mDiseCover=null;mDiscObjectAnimator=null;mJukeBoxCoverFgSize=0;mJukeBoxCoverFgSize=0;
    }
}