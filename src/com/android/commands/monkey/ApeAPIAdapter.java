package com.android.commands.monkey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.android.commands.monkey.ape.utils.Logger;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;

/**
 * Use reflection to simplify compilation.
 * @author txgu
 *
 */
public class ApeAPIAdapter {

    static Method findMethod(Class<?> clazz, String name, Class<?>... types) {
        Method method = null;
        try {
            method = clazz.getMethod(name, types);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
        } catch (java.lang.NoSuchMethodError e) {
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return method;
    }

    static PermissionInfo getPermissionInfo(IPackageManager ipm, String perm, int flags) {
        // PermissionInfo pi = mPm.getPermissionInfo(perm, 0);
        Class<?> clazz = ipm.getClass();
        String name = "getPermissionInfo";
        Method method = findMethod(clazz, name, String.class, int.class);
        if (method != null) {
            return (PermissionInfo) invoke(method, ipm, perm, flags);
        }
        method = findMethod(clazz, name, String.class, String.class, int.class);
        if (method != null) {
            return (PermissionInfo) invoke(method, ipm, perm, "shell", flags);
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
        return null;
    }

    static void registerReceiver(IActivityManager am, IIntentReceiver receiver, IntentFilter filter, int userId) {
        // registerReceiver(IApplicationThread, String, IIntentReceiver,
        // IntentFilter, String, int)
        Class<?> clazz = am.getClass();
        String name = "registerReceiver";
        Method method = findMethod(clazz, name, IApplicationThread.class, String.class, IIntentReceiver.class,
                IntentFilter.class, String.class, int.class);
        if (method != null) {
            invoke(method, am, null, null, receiver, filter, null, userId);
            return;
        }
        method = findMethod(clazz, name, IApplicationThread.class, String.class, IIntentReceiver.class,
                IntentFilter.class, String.class, int.class, boolean.class);
        if (method != null) {
            invoke(method, am, null, null, receiver, filter, null, userId, false);
            return;
        }
        method = findMethod(clazz, name, IApplicationThread.class, String.class, IIntentReceiver.class,
                IntentFilter.class, String.class, int.class, int.class);
        if (method != null) {
            invoke(method, am, null, null, receiver, filter, null, userId, 0);
            return;
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
    }

    static IActivityManager getActivityManager() {
        {
            Class<?> clazz = ActivityManagerNative.class;
            String name = "getDefault";
            Method method = findMethod(clazz, name);
            if (method != null) {
                return (IActivityManager) invoke(method, null);
            }
        }
        {
            Class<?> clazz = ActivityManager.class;
            String name = "getService";
            Method method = findMethod(clazz, name);
            if (method != null) {
                return (IActivityManager) invoke(method, null);
            }
        }
        Logger.println("Cannot getActivityManager");
        System.exit(1);
        return null;
    }

    static Object invoke(Method method, Object reciver, Object... args) {
        try {
            return method.invoke(reciver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static List<ResolveInfo> queryIntentActivities(IPackageManager mPm, Intent intent, int userId)
            throws RemoteException {
        Object result = null;
        {
            Class<?> clazz = mPm.getClass();
            String name = "queryIntentActivities";
            Method method = findMethod(clazz, name, Intent.class, String.class, int.class, int.class);
            if (method == null) {
                Logger.println("Cannot resolve method: " + name);
                System.exit(1);
                return null;
            }
            result = invoke(method, mPm, intent, null, 0, userId);
        }
        // Object result = mPm.queryIntentActivities(intent, null, 0, userId);
        if (result == null) {
            return null;
        }
        if (List.class.isInstance(result)) {
            return (List<ResolveInfo>) result;
        }
        Class<?> clazz = result.getClass();
        String name = "getList";
        Method method = findMethod(clazz, name);
        if (method != null) {
            return (List<ResolveInfo>) invoke(method, result);
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
        return null;
    }

    static void setActivityController(IActivityManager mAm, Object controller) {
        Class<?> clazz = mAm.getClass();
        String name = "setActivityController";
        Method method = findMethod(clazz, name, android.app.IActivityController.class);
        if (method != null) {
            invoke(method, mAm, controller);
            return;
        }
        method = findMethod(clazz, name, android.app.IActivityController.class, boolean.class);
        if (method != null) {
            invoke(method, mAm, controller, true);
            return;
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
    }
}
