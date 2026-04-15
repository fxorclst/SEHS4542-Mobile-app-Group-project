package com.group.groupProject.game.minggame;

import android.content.Context;

public final class UiUtils {

    private UiUtils() {
        // Utility class.
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

