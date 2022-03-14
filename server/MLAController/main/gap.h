#ifndef _GAP_H
#define _GAP_H

#define GAP_TAG "MLA_CONTROLLER_GAP" // Used for logging only

void gap_event_handler(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param);

#endif /*_GAP_H*/