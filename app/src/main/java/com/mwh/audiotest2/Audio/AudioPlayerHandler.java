package com.mwh.audiotest2.Audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

/**
 * Created by KingFish on 2017/12/27.
 */

/**
 * 实时音频播放类
 * <pre>
 * audioPlayerHandler = new AudioPlayerHandler();
 * audioPlayerHandler.prepare();// 播放前需要prepare。可以重复prepare
 * // 直接将需要播放的数据传入即可
 * audioPlayerHandler.onPlaying(data, 0, data.length);
 * </pre>
 */
public class AudioPlayerHandler implements Runnable {

    private AudioTrack track = null; //录音文件播放对象

    private boolean isPlaying = false; //是否正在录音
    private int frequence = 8000; //采样率 8000
    private int channelInConfig = AudioFormat.CHANNEL_OUT_MONO; //采样为双声道
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; //音频编码（16位）
    private int bufferSize = -1; //播放缓冲大小
    private LinkedBlockingDeque<Object> dataQueue = new LinkedBlockingDeque<>(); //音频数据
    private Semaphore semaphore = new Semaphore(1); //互斥信号量
    private boolean release = false; //是否释放资源的标志位

    public AudioPlayerHandler() {
        //获取缓冲大小
        bufferSize = AudioTrack.getMinBufferSize(frequence, channelInConfig,
                audioEncoding);
        //实例化AudioTrack
        track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence,
                channelInConfig, audioEncoding, bufferSize,
                AudioTrack.MODE_STREAM);
        track.setStereoVolume(AudioTrack.getMaxVolume(),
                AudioTrack.getMaxVolume());
        try {
            //默认抢占一个信号量，防止播放进程执行
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //开启播放线程
        new Thread(this).start();
    }

    /**
     * 当有新数据传入时，播放
     * @param data 语音byte数组
     * @param startIndex 开始的偏移量
     * @param length 数据长度
     */
    public synchronized void onPlaying(byte[] data, int startIndex, int length) {
        if (AudioTrack.ERROR_BAD_VALUE == bufferSize)
            return; //初始化错误
        try {
            dataQueue.putLast(data);
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 准备播放
     */
    public void prepare() {
        if (track != null && !isPlaying) {
            track.play();
            isPlaying = true;
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (track != null) {
            track.stop();
            isPlaying = false;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        release = true;
        semaphore.release();
        if (track != null) {
            track.release();
            track = null;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (release)
                return;
            //当有音频数据时，播放
            if (dataQueue.size() > 0) {
                byte[] data = (byte[]) dataQueue.pollFirst();
                track.write(data, 0, data.length);
            }
            else {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
