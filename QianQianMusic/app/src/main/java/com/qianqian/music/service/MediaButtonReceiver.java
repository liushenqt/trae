package com.qianqian.music.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                Intent serviceIntent = new Intent(context, MusicService.class);
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        serviceIntent.setAction(MusicService.ACTION_PLAY_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        serviceIntent.setAction(MusicService.ACTION_NEXT);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        serviceIntent.setAction(MusicService.ACTION_PREVIOUS);
                        break;
                    default:
                        return;
                }
                context.startService(serviceIntent);
            }
        }
    }
}
