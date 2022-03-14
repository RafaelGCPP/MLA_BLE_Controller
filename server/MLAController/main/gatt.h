#ifndef _GATT_H
#define _GATT_H

#define GATTS_TAG "MLA_CONTROLLER_GATT" // Used for logging only
#define PROFILE_NUM 1
#define PROFILE_MLA_APP_ID 0

extern gatts_profile_inst gl_profile_tab[PROFILE_NUM];

void gatts_event_handler(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param);

#endif /*_GATT_H*/