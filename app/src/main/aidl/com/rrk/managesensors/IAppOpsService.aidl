package com.rrk.managesensors;

interface IAppOpsService {
    void setAppOps(String packageName, String permission, boolean enabled) = 1;
    boolean getAppOps(String packageName, String permission) = 2;
} 