#ifndef _BLE_CONFIG_H
#define _BLE_CONFIG_H

#define adv_config_flag (1 << 0)
#define scan_rsp_config_flag (1 << 1)

extern prepare_type_env_t a_prepare_write_env;
extern esp_ble_adv_data_t adv_data;
extern esp_ble_adv_data_t scan_rsp_data;
extern esp_ble_adv_params_t adv_params;
extern uint8_t adv_config_done;
extern uint8_t adv_service_uuid128[32];

#endif /* _BLE_CONFIG_H */