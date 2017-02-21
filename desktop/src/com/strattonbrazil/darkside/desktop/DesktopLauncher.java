package com.strattonbrazil.darkside.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.strattonbrazil.darkside.DarksideGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        if (true) {
            config.width = 1700;
            config.height = 900;
        }
        new LwjglApplication(new DarksideGame(), config);
    }
}
