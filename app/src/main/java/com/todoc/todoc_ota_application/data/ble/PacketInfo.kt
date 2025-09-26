package com.todoc.todoc_ota_application.data.ble

object PacketInfo {
    val GAIA_HEADER: ByteArray = byteArrayOf(0x19, 0x04, 0x02, 0x01)        //1904 company id
    val GAIA_HEADER_RESPONSE: ByteArray = byteArrayOf(0x19, 0x04, 0x03, 0x01)        //1904 company id

    const val GAIA_HEADER_PAIRING_REQUEST: Byte = (0x80 and 0xff).toByte()
    const val GAIA_PAYLOAD_PAIRING_REQUEST: Byte = (0x80 and 0xff).toByte()

    const val GAIA_HEADER_CONNECT_STATE_REQUEST: Byte = (0x81 and 0xff).toByte()
    const val GAIA_PAYLOAD_CONNECT_STATE_REQUEST: Byte = (0x81 and 0xff).toByte()

    const val GAIA_HEADER_BLUETOOTH_VERSION_INFO_REQUEST: Byte = (0x82 and 0xff).toByte()
    const val GAIA_PAYLOAD_BLUETOOTH_VERSION_INFO_REQUEST: Byte = (0x82 and 0xff).toByte()

    const val GAIA_HEADER_BLUETOOTH_BUILD_INFO_REQUEST: Byte = (0x83 and 0xff).toByte()
    const val GAIA_PAYLOAD_BLUETOOTH_BUILD_INFO_REQUEST: Byte = (0x83 and 0xff).toByte()


    const val HEADER_PASSWORD: Byte = (0x40 and 0xff).toByte()
    const val HEADER_SOUND_PROCESSOR_INFO: Byte = (0x41 and 0xff).toByte()
    const val HEADER_MAP_DATA_INFORMATION: Byte = (0x42 and 0xff).toByte()
    const val HEADER_SOUND_PROCESSOR_STATUS: Byte = (0x43 and 0xff).toByte()
    const val HEADER_VALUE_PROMGRAM: Byte = (0x44 and 0xff).toByte()
    const val HEADER_VALUE_MAX_OUTPUT: Byte = (0x45 and 0xff).toByte()
    const val HEADER_VALUE_VOLUME: Byte = (0x46 and 0xff).toByte()
    const val HEADER_VALUE_TELECOIL: Byte = (0x47 and 0xff).toByte()
    const val HEADER_VALUE_NOTIFICATION: Byte = (0x48 and 0xff).toByte()
    const val HEADER_VALUE_LED: Byte = (0x49 and 0xff).toByte()
//    const val HEADER_READ_ISD_ID_AND_USER: Byte = (0x4B and 0xff).toByte()
//    const val HEADER_READ_MAP_DATA: Byte = (0x4C and 0xff).toByte()
//    const val HEADER_WRITE_ISD_ID_AND_USER: Byte = (0x4D and 0xff).toByte()
//    const val HEADER_WRITE_MAP_DATA: Byte = (0x4E and 0xff).toByte()
//    const val HEADER_MAP_RESET_DEFAULT: Byte = (0x51 and 0xff).toByte()

    const val HEADER_READ_ISD_ID_AND_USER: Byte = (0x4A and 0xff).toByte()
    const val HEADER_READ_MAP_DATA: Byte = (0x4B and 0xff).toByte()
//    const val HEADER_WRITE_ISD_ID_AND_USER: Byte = (0x4C and 0xff).toByte()
    const val HEADER_WRITE_ISD_ID_AND_USER: Byte = (0x6C and 0xff).toByte()
//    const val HEADER_WRITE_MAP_DATA: Byte = (0x4D and 0xff).toByte()
    const val HEADER_WRITE_MAP_DATA: Byte = (0x6D and 0xff).toByte()
//    const val HEADER_DELETE_ALL_SELECT_SLOT_MAP_DATA: Byte = (0x4E and 0xff).toByte()
    const val HEADER_DELETE_ALL_SELECT_SLOT_MAP_DATA: Byte = (0x6E and 0xff).toByte()
//    const val HEADER_DELETE_SELECT_SLOT_MAP_DATA: Byte = (0x4F and 0xff).toByte()
    const val
        HEADER_DELETE_SELECT_SLOT_MAP_DATA: Byte = (0x6F and 0xff).toByte()


    const val HEADER_INIT_MAP_DATA_COMMAND: Byte = (0x71 and 0xff).toByte()

    const val HEADER_MAP_RESET_DEFAULT: Byte = (0x50 and 0xff).toByte()
    const val HEADER_READ_EXTERNAL_USER: Byte = (0x51 and 0xff).toByte()

    const val HEADER_ERROR_MAP_DATA: Byte = (0xF0 and 0xff).toByte()


    const val HEADER_AUDIO_INPUT_MAX_READ: Byte = (0x52 and 0xff).toByte() // 4A -> 52 [2024.07.08]
    const val HEADER_READ_NRF_CODE: Byte = (0x53 and 0xff).toByte()
    const val HEADER_READ_EZAIRO_CODE: Byte = (0x54 and 0xff).toByte()
    const val HEADER_WRITE_MANUFACTURE_NUM: Byte = (0x55 and 0xff).toByte()
    const val HEADER_WRITE_PAIRING_KEY: Byte = (0x56 and 0xff).toByte()
    const val HEADER_INIT_MAPPING_DATA: Byte = (0x57 and 0xff).toByte()
    const val HEADER_ERROR: Byte = (0x58 and 0xff).toByte()


    const val HEADER_START_COMMAND: Byte = (0xC0 and 0xff).toByte()
    const val HEADER_END_COMMAND: Byte = (0xC1 and 0xff).toByte()
    const val HEADER_INFO_SELECT_COMMAND: Byte = (0xC2 and 0xff).toByte()
    const val INDEX_INFO_COMMAND: Byte = (0x01 and 0xff).toByte()
    const val INDEX_SELECT_COMMAND: Byte = (0x02 and 0xff).toByte()

    const val HEADER_WRITE_COMMAND: Byte = (0xC3 and 0xff).toByte()
    const val INDEX_CHOOSE_FILE_HEADER_COMMAND: Int = 0
    const val INDEX_WRITE_COMMAND: Byte = (0x02 and 0xff).toByte()

    const val HEADER_ERROR_COMMAND: Byte = (0xC8 and 0xff).toByte()


    //Start OTA 응답 별
    const val START_RESULT_ACCEPT: Byte = (0x00 and 0xff).toByte()
    const val START_RESULT_ERR_EZAIRO_COMM: Byte = (0x01 and 0xff).toByte()
    const val START_RESULT_ERR_LOW_BATTERY: Byte = (0x02 and 0xff).toByte()
    const val START_RESULT_ERR_UNKNOWN: Byte = (0xFF and 0xff).toByte()

    //END OTA 응답 별
    const val END_RESULT_ACCEPT: Byte = (0x00 and 0xff).toByte()


    //INFO 응답 별
    const val INFO_RESULT_ACCEPT: Byte = (0x01 and 0xff).toByte()
    const val INFO_RESULT_REJECT_INVALID_SLOT_NUM: Byte = (0x02 and 0xff).toByte()
    const val INFO_RESULT_REJECT_CURRENT_BOOT_SLOT: Byte = (0x03 and 0xff).toByte()

    //SELECT 응답 별
    const val SELECT_RESULT_ACCEPT: Byte = (0x01 and 0xff).toByte()
    const val SELECT_RESULT_REJECT_INVALID_SLOT_NUM: Byte = (0x02 and 0xff).toByte()
    const val SELECT_RESULT_REJECT_CURRENT_BOOT_SLOT: Byte = (0x03 and 0xff).toByte()

    //WRITE INDEX0 응답 별
    const val WRITE_0_RESULT_ACCEPT: Byte = (0x01 and 0xff).toByte()
    const val WRITE_0_RESULT_INVALID_DATA_INDEX_ORDER: Byte = (0x02 and 0xff).toByte()
    const val WRITE_0_RESULT_ERR_CURRENT_BOOT_SLOT: Byte = (0x03 and 0xff).toByte()
    const val WRITE_0_RESULT_NO_FILE_EXIST: Byte = (0x04 and 0xff).toByte()

    //WRITE 응답 별
    const val WRITE_RESULT_ACCEPT: Byte = (0x01 and 0xff).toByte()
    const val WRITE_RESULT_INVALID_DATA_INDEX_ORDER: Byte = (0x02 and 0xff).toByte()
    const val WRITE_RESULT_ERR_CURRENT_BOOT_SLOT: Byte = (0x03 and 0xff).toByte()

    //ERROR 응답 별
    const val ERROR_RESULT_NONE: Byte = (0x00 and 0xff).toByte()
    const val ERROR_RESULT_EZAIRO_COMM_ERROR: Byte = (0x01 and 0xff).toByte()
    const val ERROR_RESULT_LOW_BATTERY: Byte = (0x02 and 0xff).toByte()
    const val ERROR_RESULT_NOT_OTA_MODE: Byte = (0x03 and 0xff).toByte()
    const val ERROR_RESULT_INVALID_OTA_ORDER: Byte = (0x04 and 0xff).toByte()
    const val ERROR_RESULT_UNKNOWN_ERROR: Byte = (0xFF and 0xff).toByte()



    // 응답 패킷별 사이즈 정보
    const val PACKET_SIZE_GAIA_HEADER = 4
    const val PACKET_SIZE_PASSWORD = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_PROCESSOR_INFO = 15 + PACKET_SIZE_GAIA_HEADER // 9 -> 15 변경
    const val PACKET_SIZE_MAP_DATA_INFO = 7 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_PROCESSOR_STATUS = 8 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_PROGRAM = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_MAX_OUTPUT = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_VOLUME = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_TELECOIL = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_NOTIFICATION = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_LED = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_AUDIO_INPUT_MAX_READ = 15 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_NRF_CODE = 15 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_EZAIRO_CODE = 15 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_MANUFACTURE_CODE = 5 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_PAIRING_KEY_CODE = 7 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_DATA_RESET = 2 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_SYSTEM_WARNING = 1 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_ERROR = 3 + PACKET_SIZE_GAIA_HEADER

    const val PACKET_SIZE_MAP_READ_ISD_ID_AND_USER_1 = 20 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_MAP_READ_ISD_ID_AND_USER_2 = 20 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_MAP_READ_ISD_ID_AND_USER_3 = 12 + PACKET_SIZE_GAIA_HEADER
    const val PACKET_SIZE_SOUND_PROCESSING_PARAM_INDEX_1: Int = 13 + PACKET_SIZE_GAIA_HEADER


    const val SOUND_PRECESSING_PARAM_INDEX_MIN: Int = 1
    const val SOUND_PRECESSING_PARAM_INDEX_MAX: Int = 7


    const val INIT_VALUE_BATTERY = 0
    const val INIT_VALUE_PROGRAM = 1
    const val INIT_VALUE_VOLUME = 1
    const val INIT_VALUE_MAX_OUTPUT = 1
    const val INIT_VALUE_LED = 2
    const val INIT_VALUE_NOTIFICATION = 2
    const val INIT_VALUE_TELECOIL = 2

    const val LIMIT_MIN_BATTERY = 0
    const val LIMIT_MIN_PROGRAM = 1
    const val LIMIT_MIN_VOLUME = 1
    const val LIMIT_MIN_MAX_OUTPUT = 1
    const val LIMIT_MIN_LED = 1
    const val LIMIT_MIN_NOTIFICATION = 1
    const val LIMIT_MIN_TELECOIL = 1

    const val LIMIT_MAX_BATTERY = 100
    const val LIMIT_MAX_PROGRAM =
        4 // currently max : 4, but this value can be change dynamically during running app.
    const val LIMIT_MAX_VOLUME = 10
    const val LIMIT_MAX_MAX_OUTPUT = 4
    const val LIMIT_MAX_LED = 2
    const val LIMIT_MAX_NOTIFICATION = 2
    const val LIMIT_MAX_TELECOIL = 2

    const val PASSWORD_PASS: Byte = 1
    const val PASSWORD_FAIL: Byte = 2

    const val NOTIFICATION_ON: Byte = 1
    const val NOTIFICATION_OFF: Byte = 2

    const val LED_ON: Byte = 1
    const val LED_OFF: Byte = 2

    const val TELECOIL_ON: Byte = 1
    const val TELECOIL_OFF: Byte = 2

    const val MAX_OUTPUT_UP: Byte = 1
    const val MAX_OUTPUT_DOWN: Byte = 2

    const val VOLUME_UP: Byte = 1
    const val VOLUME_DOWN: Byte = 2

    const val PROGRAM_UP: Byte = 1
    const val PROGRAM_DOWN: Byte = 2

    const val SLOT_MIN = 1
    const val SLOT_MAX = 4

    const val ID_AND_USER_INDEX_MIN: Byte = 1
    const val ID_AND_USER_INDEX_MAX = 3

    const val MAP_DATA_INDEX_MIN = 1
    const val MAP_DATA_INDEX_MAX = 15
    const val MAP_DATA_INIT_PAYLOAD: Byte = 1

    const val MAP_RESET_DEFAULT_OK = 1

    var battery: Byte = INIT_VALUE_BATTERY.toByte()
    var program: Byte = INIT_VALUE_PROGRAM.toByte()
    var volume: Byte = INIT_VALUE_VOLUME.toByte()
    var maxOutput: Byte = INIT_VALUE_MAX_OUTPUT.toByte()
    var led: Byte = INIT_VALUE_LED.toByte()
    var notification: Byte = INIT_VALUE_NOTIFICATION.toByte()
    var telecoil: Byte = INIT_VALUE_TELECOIL.toByte()
    var error: Byte = 0

    const val SLOT_NUM_1: Byte = 1
    const val SLOT_NUM_2: Byte = 2
    const val SLOT_NUM_3: Byte = 3
    const val SLOT_NUM_4: Byte = 4

    val INFO_COMMAND_BYTEARRAY = byteArrayOf(INDEX_INFO_COMMAND)
    val MAP_COMMAND_IDS = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F)


//    override fun toString(): String {
//        return "DeviceParam(battery=$battery, program=$program, volume=$volume, maxOutput=$maxOutput, alarmLed=$led, alarmStimulation=$notification, telecoil=$telecoil)"
//    }
}
