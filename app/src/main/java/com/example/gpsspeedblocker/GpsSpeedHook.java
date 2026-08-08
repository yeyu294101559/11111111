package com.example.gpsspeedblocker;

import android.location.Location;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Legacy Xposed entry point. LSPosed keeps compatibility with this API and
 * exposes the module through app/src/main/assets/xposed_init.
 */
public final class GpsSpeedHook implements IXposedHookLoadPackage {
    private static final String PREFS_NAME = "settings";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SPEED = "speed_mps";
    private static final String KEY_PACKAGES = "target_packages";

    private static volatile XSharedPreferences preferences;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)
                || "android".equals(lpparam.packageName)) {
            return;
        }

        try {
            Class<?> locationClass = XposedHelpers.findClass(
                    "android.location.Location",
                    lpparam.classLoader
            );

            hookLocationMethod(lpparam.packageName, locationClass, "getSpeed", new ResultProvider() {
                @Override
                public Object get() {
                    return readConfig().speedMps;
                }
            });
            hookLocationMethod(lpparam.packageName, locationClass, "hasSpeed", new ResultProvider() {
                @Override
                public Object get() {
                    return true;
                }
            });
            hookLocationMethod(lpparam.packageName, locationClass, "getSpeedAccuracyMetersPerSecond", new ResultProvider() {
                @Override
                public Object get() {
                    return 0.0f;
                }
            });
            hookLocationMethod(lpparam.packageName, locationClass, "hasSpeedAccuracy", new ResultProvider() {
                @Override
                public Object get() {
                    return true;
                }
            });

            XposedBridge.log("GPSSpeedBlocker hooked " + lpparam.packageName);
        } catch (Throwable throwable) {
            XposedBridge.log("GPSSpeedBlocker failed for "
                    + lpparam.packageName + ": " + throwable);
        }
    }

    private static void hookLocationMethod(
            final String packageName,
            Class<?> locationClass,
            String methodName,
            final ResultProvider resultProvider
    ) {
        try {
            XposedBridge.hookAllMethods(locationClass, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Config config = readConfig();
                    if (!config.enabled || !config.matchesPackage(packageName)) {
                        return;
                    }
                    param.setResult(resultProvider.get());
                }
            });
        } catch (Throwable throwable) {
            // Some methods only exist on newer Android releases.
            XposedBridge.log("GPSSpeedBlocker skipped " + methodName + ": " + throwable);
        }
    }

    private interface ResultProvider {
        Object get();
    }

    private static Config readConfig() {
        XSharedPreferences prefs = preferences;
        if (prefs == null) {
            synchronized (GpsSpeedHook.class) {
                prefs = preferences;
                if (prefs == null) {
                    prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, PREFS_NAME);
                    prefs.makeWorldReadable();
                    preferences = prefs;
                }
            }
        }

        prefs.reload();
        return new Config(
                prefs.getBoolean(KEY_ENABLED, false),
                clampSpeed(prefs.getFloat(KEY_SPEED, 0.0f)),
                prefs.getString(KEY_PACKAGES, "")
        );
    }

    private static float clampSpeed(float speed) {
        if (Float.isNaN(speed) || Float.isInfinite(speed)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(speed, 343.0f));
    }

    private static final class Config {
        private final boolean enabled;
        private final float speedMps;
        private final String targetPackages;

        private Config(boolean enabled, float speedMps, String targetPackages) {
            this.enabled = enabled;
            this.speedMps = speedMps;
            this.targetPackages = targetPackages == null ? "" : targetPackages.trim();
        }

        private boolean matchesPackage(String packageName) {
            if (targetPackages.isEmpty()) {
                return true;
            }
            String[] candidates = targetPackages.split("[,;\\s]+");
            for (String candidate : candidates) {
                if (candidate.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
