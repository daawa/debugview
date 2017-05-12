package io.github.daawa.debug;
/**
 * Created by daawa on 16/9/13.
 */
public interface DebugMonitor {
    void requestDescription(String reqInfo);
    void responseDescription(String jsonString);
    void crashDes(String des);
}