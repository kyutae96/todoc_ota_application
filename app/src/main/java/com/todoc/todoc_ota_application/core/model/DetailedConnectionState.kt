package com.todoc.todoc_ota_application.core.model

enum class DetailedConnectionState {
    Disconnected,
    Connecting,
    Connected,
    ServiceDiscovering,
    ServiceDiscovered,
    DescriptorSetting,
    DescriptorSet,
    NotificationEnabled,
    FullyReady
}
