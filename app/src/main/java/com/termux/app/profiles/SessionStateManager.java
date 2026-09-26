package com.termux.app.profiles;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Session状态管理器
 * 处理session状态的持久化和恢复
 */
public class SessionStateManager {

    private static final String TAG = "SessionStateManager";
    private static final String SESSIONS_FILE = "sessions.json";

    private final Context context;
    private final File sessionsDir;

    public SessionStateManager(Context context) {
        this.context = context.getApplicationContext();
        this.sessionsDir = new File(this.context.getFilesDir(), "sessions");
    }

    /**
     * 获取sessions存储目录
     * @return sessions目录
     */
    public File getSessionsDir() {
        return sessionsDir;
    }

    /**
     * 保存session状态
     * @param sessionId Session ID
     * @param profileId Profile ID
     * @param workingDirectory 工作目录
     * @return 是否成功
     */
    public boolean saveSessionState(String sessionId, String profileId, String workingDirectory) {
        if (sessionId == null || profileId == null) {
            return false;
        }

        List<SessionState> states = loadSessionStates();

        // 检查是否已存在
        for (SessionState state : states) {
            if (state.sessionId.equals(sessionId)) {
                // 更新现有状态
                state.profileId = profileId;
                state.workingDirectory = workingDirectory;
                state.lastActiveAt = System.currentTimeMillis();
                return saveSessionStates(states);
            }
        }

        // 创建新状态
        SessionState newState = new SessionState();
        newState.sessionId = sessionId;
        newState.profileId = profileId;
        newState.workingDirectory = workingDirectory;
        newState.createdAt = System.currentTimeMillis();
        newState.lastActiveAt = System.currentTimeMillis();
        states.add(newState);

        return saveSessionStates(states);
    }

    /**
     * 加载所有session状态
     * @return Session状态列表
     */
    public List<SessionState> loadSessionStates() {
        List<SessionState> states = new ArrayList<>();

        // 确保目录存在
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs();
            return states;
        }

        File sessionsFile = new File(sessionsDir, SESSIONS_FILE);
        if (!sessionsFile.exists()) {
            return states;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(sessionsFile);
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }

            JSONObject json = new JSONObject(content.toString());
            JSONArray sessionsArray = json.optJSONArray("sessions");

            if (sessionsArray != null) {
                for (int i = 0; i < sessionsArray.length(); i++) {
                    try {
                        JSONObject sessionJson = sessionsArray.getJSONObject(i);
                        SessionState state = new SessionState();
                        state.sessionId = sessionJson.getString("sessionId");
                        state.profileId = sessionJson.getString("profileId");
                        state.workingDirectory = sessionJson.optString("workingDirectory", null);
                        state.createdAt = sessionJson.optLong("createdAt", System.currentTimeMillis());
                        state.lastActiveAt = sessionJson.optLong("lastActiveAt", System.currentTimeMillis());
                        states.add(state);
                    } catch (JSONException e) {
                        Log.w(TAG, "Error parsing session state at index " + i, e);
                    }
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading sessions file", e);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing sessions JSON", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing sessions file", e);
                }
            }
        }

        return states;
    }

    /**
     * 保存所有session状态
     * @param states Session状态列表
     * @return 是否成功
     */
    private boolean saveSessionStates(List<SessionState> states) {
        // 确保目录存在
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs();
        }

        FileWriter writer = null;
        try {
            JSONObject json = new JSONObject();
            JSONArray sessionsArray = new JSONArray();

            for (SessionState state : states) {
                JSONObject sessionJson = new JSONObject();
                sessionJson.put("sessionId", state.sessionId);
                sessionJson.put("profileId", state.profileId);
                sessionJson.put("workingDirectory", state.workingDirectory);
                sessionJson.put("createdAt", state.createdAt);
                sessionJson.put("lastActiveAt", state.lastActiveAt);
                sessionsArray.put(sessionJson);
            }

            json.put("sessions", sessionsArray);

            File sessionsFile = new File(sessionsDir, SESSIONS_FILE);
            writer = new FileWriter(sessionsFile);
            writer.write(json.toString(2));
            writer.flush();

            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating sessions JSON", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error writing sessions file", e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing sessions file", e);
                }
            }
        }
    }

    /**
     * 移除session状态（手动关闭时调用）
     * @param sessionId Session ID
     * @return 是否成功
     */
    public boolean removeSessionState(String sessionId) {
        if (sessionId == null) {
            return false;
        }

        List<SessionState> states = loadSessionStates();
        boolean removed = false;

        Iterator<SessionState> iterator = states.iterator();
        while (iterator.hasNext()) {
            SessionState state = iterator.next();
            if (state.sessionId.equals(sessionId)) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (removed) {
            return saveSessionStates(states);
        }

        return false;
    }

    /**
     * 清除所有session状态
     * @return 是否成功
     */
    public boolean clearAllSessionStates() {
        List<SessionState> emptyStates = new ArrayList<>();
        return saveSessionStates(emptyStates);
    }

    /**
     * 获取要恢复的sessions（非手动关闭的）
     * @return 要恢复的Session状态列表
     */
    public List<SessionState> getSessionsToRestore() {
        // 这里可以添加逻辑来判断哪些sessions需要恢复
        // 目前返回所有sessions，由调用方决定是否恢复
        return loadSessionStates();
    }

    /**
     * Session状态数据类
     */
    public static class SessionState {
        public String sessionId;
        public String profileId;
        public String workingDirectory;
        public long createdAt;
        public long lastActiveAt;

        @Override
        public String toString() {
            return "SessionState{" +
                   "sessionId='" + sessionId + '\'' +
                   ", profileId='" + profileId + '\'' +
                   ", workingDirectory='" + workingDirectory + '\'' +
                   '}';
        }
    }
}
