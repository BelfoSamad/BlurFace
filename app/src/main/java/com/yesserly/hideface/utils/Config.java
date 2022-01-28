package com.yesserly.hideface.utils;

import com.yesserly.hideface.R;
import com.yesserly.hideface.pojo.Sticker;

public class Config {

    public static boolean enableGDPR = true;

    public static boolean enableAds = true;

    public static int[] slideImages = {
            R.drawable.people_1,
            R.drawable.people_2,
            R.drawable.people_3
    };

    public static Sticker[] stickers = {
            new Sticker(R.drawable.smile),
            new Sticker(R.drawable.laugh_1),
            new Sticker(R.drawable.laugh_2),
            new Sticker(R.drawable.sunglasses),
            new Sticker(R.drawable.evil),
            new Sticker(R.drawable.shy),
            new Sticker(R.drawable.love)
    };

}
