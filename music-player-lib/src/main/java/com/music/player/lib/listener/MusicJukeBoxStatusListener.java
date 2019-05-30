package com.music.player.lib.listener;

import android.view.View;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicLrcRow;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/6
 * Music JukeBox Status Listener
 */

public interface MusicJukeBoxStatusListener {

    /**
     * 手指横向滚动过程对象变化
     * @param audioInfo 音频对象
     */
    void onJukeBoxOffsetObject(BaseAudioInfo audioInfo);

    /**
     * 音频对象切换了
     * @param position 索引
     * @param musicPicRes 音频对象
     * @param isEchoDisplay 是否回显
     */
    void onJukeBoxObjectChanged(int position, BaseAudioInfo musicPicRes, boolean isEchoDisplay);

    /**
     * 唱片机状态发生了变化
     * @param playerState 唱片机状态,参见MusicConstants常量定义
     */
    void onJukeBoxState(int playerState);

    /**
     * 唱片机点击事件,主要抛出给组件来控制歌词控件
     * @param view click view
     */
    void onClickJukeBox(View view);

    /**
     * 歌词被拖动了，跳转至指定位置开始播放
     * @param lrcRow 歌词对象
     */
    void onLrcSeek(MusicLrcRow lrcRow);
}