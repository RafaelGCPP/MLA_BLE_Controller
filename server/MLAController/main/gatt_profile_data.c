#include "esp_gatts_api.h"
#include "ble_types.h"
#include "gatt_profile.h"
#include "esp_bt_defs.h"

gatts_profile_inst gl_profile_tab[PROFILE_NUM] = {
    [PROFILE_MLA_APP_ID] = {
        .gatts_cb = gatts_profile_mla_event_handler,
        .gatts_if = ESP_GATT_IF_NONE, /* Not get the gatt_if, so initial is ESP_GATT_IF_NONE */
    }};

esp_gatt_char_prop_t a_property = 0;

uint8_t char1_str[] = {0x11, 0x22, 0x33};
esp_attr_value_t gatts_demo_char1_val =
    {
        .attr_max_len = GATTS_DEMO_CHAR_VAL_LEN_MAX,
        .attr_len = sizeof(char1_str),
        .attr_value = char1_str,
};

prepare_type_env_t a_prepare_write_env;
