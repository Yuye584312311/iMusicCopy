package com.music.player.lib.manager;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicWindowMiniJukebox;
import com.music.player.lib.view.MusicWindowTrash;
import java.lang.reflect.Method;

/**
 * TinyHung@Outlook.com
 * 2019/3/12
 * WindowManager
 */

public class MusicWindowManager {

	private static final String TAG = "MusicWindowManager";
	private static volatile MusicWindowManager mInstance;
	//迷你唱片机
	private MusicWindowMiniJukebox mMusicWindowMiniJukebox;
	private MusicWindowTrash mMusicWindowTrash;
	private static WindowManager mWindowManager;

    public static MusicWindowManager getInstance() {
        if(null==mInstance){
            synchronized (MusicWindowManager.class) {
                if (null == mInstance) {
                    mInstance = new MusicWindowManager();
                }
            }
        }
		return mInstance;
	}

	private MusicWindowManager(){}

	/**
	 * 添加一个View到窗口
	 * @param context
	 */
	public synchronized MusicWindowManager createMiniJukeBoxToWindown(Context context) {
        createMiniJukeBoxToWindown(context,-1,-1);
		return mInstance;
	}

    /**
     * 添加一个View到窗口,默认位置是位于屏幕左上角，自行指定X、Y轴偏移量
     * @param context 全局上下文
     * @param offsetPixelX X轴偏移量 单位像素 -1:使用默认
     * @param offsetPixelY Y轴偏移量 单位像素 -1:使用默认
     */
	public synchronized void createMiniJukeBoxToWindown(Context context, int offsetPixelX, int offsetPixelY) {
        if(!isWindowShowing()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse( "package:"+MusicUtils.getInstance().getPackageName(context)));
                    context.startActivity(intent);
                } else {
                    addMiniJukeBoxToWindown(context,offsetPixelX,offsetPixelY);
                }
            }else {
                addMiniJukeBoxToWindown(context,offsetPixelX,offsetPixelY);
            }
        }
	}

    /**
     * 添加一个View到窗口,默认位置是位于屏幕左上角，自行指定X、Y轴偏移量
     * @param context 全局上下文
     * @param offsetPixelX X轴偏移量 单位像素 -1:使用默认
     * @param offsetPixelY Y轴偏移量 单位像素 -1:使用默认
     */
    private synchronized void addMiniJukeBoxToWindown(Context context, int offsetPixelX, int offsetPixelY) {
        if (null== mMusicWindowMiniJukebox) {
            WindowManager windowManager = getWindowManager(context);
            int screenWidth = MusicUtils.getInstance().getScreenWidth(context);
            int screenHeight = MusicUtils.getInstance().getScreenHeight(context);
            mMusicWindowMiniJukebox = new MusicWindowMiniJukebox(context,windowManager);
            WindowManager.LayoutParams miniJukeBoxLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            //不拦截焦点、使焦点穿透到底层
            miniJukeBoxLayoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //背景透明
            miniJukeBoxLayoutParams.format = PixelFormat.RGBA_8888;
            //默认位于屏幕的左上角，具体位置定位定传X、Y偏移量
            miniJukeBoxLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            miniJukeBoxLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            miniJukeBoxLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            if(offsetPixelX>-1){
                int offsetX = screenWidth - offsetPixelX;
                miniJukeBoxLayoutParams.x=offsetX;
            }else{
                //X轴偏移量=screenWidth-(ViewHeight+marginBottomHeight)
                int dpToPxIntX = MusicUtils.getInstance().dpToPxInt(context, 24f)+ mMusicWindowMiniJukebox.getViewWidth();
                int offsetX = screenWidth - dpToPxIntX;
                miniJukeBoxLayoutParams.x=offsetX;
                Logger.d(TAG,"X:"+offsetX);
            }
            if(offsetPixelY>-1){
                int offsetY = screenHeight - offsetPixelY;
                miniJukeBoxLayoutParams.y=offsetY;

            }else{
                //Y轴偏移量=ScreenHeight-(ViewHeight+statusBarHeight+marginBottomHeight)
                int dpToPxIntY = MusicUtils.getInstance().dpToPxInt(context, 42f)+ mMusicWindowMiniJukebox.getViewHeight();
                int offsetY = screenHeight - dpToPxIntY;
                miniJukeBoxLayoutParams.y=offsetY;
                Logger.d(TAG,"Y:"+offsetY);
            }
            mMusicWindowMiniJukebox.setClipChildren(false);
            mMusicWindowMiniJukebox.setWindowManagerParams(miniJukeBoxLayoutParams);
            windowManager.addView(mMusicWindowMiniJukebox, miniJukeBoxLayoutParams);
        }
    }

    /**
	 * 将小悬浮窗从屏幕上移除。
	 * @param context 全局上下文
	 */
	public synchronized void removeMiniJukeBoxFromWindow(Context context) {
		if(null!= mMusicWindowMiniJukebox){
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(mMusicWindowMiniJukebox);
			mMusicWindowMiniJukebox = null;
		}
	}

    /**
     * 添加一个垃圾桶至窗口
     * @param context 全局上下文,这个悬浮窗层级应该比唱片机低一级，使其显示在唱片机下方
     * @return 控件的宽高，用来确定控件在屏幕的位置
     */
    public synchronized Object addMiniJukeBoxTrashToWindown(Context context) {
        if (null== mMusicWindowTrash) {
            WindowManager windowManager = getWindowManager(context);
            mMusicWindowTrash = new MusicWindowTrash(context);
            int dpToPxInt = MusicUtils.getInstance().dpToPxInt(context, 120f);
            WindowManager.LayoutParams trachLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                trachLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
                trachLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                trachLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            //不拦截焦点、使焦点穿透到底层
            trachLayoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //背景透明
            trachLayoutParams.format = PixelFormat.RGBA_8888;
            trachLayoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            trachLayoutParams.width = dpToPxInt;
            trachLayoutParams.height = dpToPxInt;
            trachLayoutParams.x=0;
            trachLayoutParams.y=0;
            windowManager.addView(mMusicWindowTrash, trachLayoutParams);
            return new int[]{dpToPxInt,dpToPxInt};
        }
        return mMusicWindowTrash;
    }

    /**
     * 悬浮窗垃圾桶从屏幕移除
     * @param context 全局上下文
     */
    public synchronized void removeTrashFromWindown(final Context context) {
        if(null!= mMusicWindowTrash){
            mMusicWindowTrash.startHideAnimation(new MusicAnimatorListener() {
                @Override
                public void onAnimationStart() {

                }

                @Override
                public void onAnimationEnd() {
                    if(null!=mMusicWindowTrash){
                        mMusicWindowTrash.onDestroy();
                        WindowManager windowManager = getWindowManager(context);
                        windowManager.removeView(mMusicWindowTrash);
                        mMusicWindowTrash = null;
                    }
                }
            });
        }
    }

    /**
     * 垃圾桶焦点感应是否捕获
     * @param focusCap 全局上下文
     */
    public void jukeBoxTrashFocusCap(boolean focusCap) {
        if(null!= mMusicWindowTrash){
            mMusicWindowTrash.jukeBoxTrashFocusCap(focusCap);
        }
    }

    /**
     * 垃圾桶抖动动画
     */
    public void startShakeAnimation() {
        if(null!= mMusicWindowTrash){
            mMusicWindowTrash.startShakeAnimation();
        }
    }

    /**
     * 开始显示垃圾桶
     */
    public void startTrashWindowAnimation(){
        if(null!= mMusicWindowTrash){
            mMusicWindowTrash.startTrashWindowAnimation();
        }
    }

    /**
     * 此应用是否拥有悬浮窗权限
     * @param context 全局上下文
     * @return 为true表示拥有悬浮窗权限
     */
	public boolean haveWindownPermission(Context context){
	    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
	        return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * @param context 全局上下文
     * @return true 允许  false禁止
     */
    public boolean checkAlertWindowsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

	/**
	 * 更新小悬浮窗的TextView上的数据，显示正在加载中
	 */
	public void updateWindowStatus(MusicStatus musicStatus) {
		if(mMusicWindowMiniJukebox !=null){
			mMusicWindowMiniJukebox.updateData(musicStatus);
		}
	}

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 窗口是否有唱片机悬浮窗口显示
	 * @return 为true标识当前窗口已经存在悬浮窗
	 */
	public boolean isWindowShowing() {
		return null!= mMusicWindowMiniJukebox;
	}

    /**
     * 窗口是否有垃圾桶
     * @return 为true表示当前窗口拥有垃圾桶
     */
    public boolean isTrashShowing() {
        return null!= mMusicWindowTrash;
    }

    /**
     * 悬浮窗中迷你唱片机动画开启
     */
    public void onResume() {
	    if(null!= mMusicWindowMiniJukebox) {
            mMusicWindowMiniJukebox.onResume();
        }
    }

    /**
     * 悬浮窗中迷你唱片机动画关闭
     */
    public void onPause() {
        if(null!= mMusicWindowMiniJukebox) {
            mMusicWindowMiniJukebox.onPause();
        }
    }

    /**
     * MINIJukeBox悬浮窗可见
     */
    public void onVisible() {
        if(null!= mMusicWindowMiniJukebox) {
            mMusicWindowMiniJukebox.onVisible();
        }
    }

    /**
     * MINIJukeBox悬浮窗可见
     * @param audioID 音频ID
     */
    public void onVisible(long audioID) {
        if(null!= mMusicWindowMiniJukebox) {
            mMusicWindowMiniJukebox.onVisible(audioID);
        }
    }

    /**
     * MINIJukeBox悬浮窗不可见
     */
    public void onInvisible() {
        if(null!= mMusicWindowMiniJukebox) {
            mMusicWindowMiniJukebox.onInvisible();
        }
    }

    /**
     * 组件中对应函数调用
     */
    public void onDestroy() {
	    if(null!= mMusicWindowMiniJukebox &&null!=mWindowManager){
            mMusicWindowMiniJukebox.onDestroy();
            mWindowManager.removeViewImmediate(mMusicWindowMiniJukebox);
            mMusicWindowMiniJukebox =null;
        }
        if(null!= mMusicWindowTrash &&null!=mWindowManager){
            mMusicWindowTrash.onDestroy();
            mWindowManager.removeViewImmediate(mMusicWindowTrash);
            mMusicWindowTrash =null;
        }
        mWindowManager=null;mInstance=null;
    }
}