package com.termux.app.profiles;

/**
 * 枚举类型，定义支持的Profile类型
 */
public enum ProfileType {
    /**
     * 本地Shell - 使用Termux默认的shell
     */
    LOCAL("local", "Local Shell"),

    /**
     * Proot Shell - 通过proot运行的Linux发行版
     */
    PROOT("proot", "Proot Shell"),

    /**
     * SSH连接 - 通过openssh连接远程服务器
     */
    SSH("ssh", "SSH Connection");

    private final String value;
    private final String displayName;

    ProfileType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 从字符串值解析ProfileType
     * @param value 字符串值
     * @return 对应的ProfileType，如果无效则返回LOCAL
     */
    public static ProfileType fromValue(String value) {
        if (value != null) {
            for (ProfileType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
        }
        return LOCAL;
    }
}
