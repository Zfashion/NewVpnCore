-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * implements com.core.unitevpn.inter.NotificationChannelImpl
-keep class * implements com.core.unitevpn.inter.NotificationImpl
-keepnames class com.core.unitevpn.obj.DefaultNotification$NotificationIconResourceException