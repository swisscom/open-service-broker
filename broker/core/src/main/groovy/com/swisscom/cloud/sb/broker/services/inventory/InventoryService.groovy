package com.swisscom.cloud.sb.broker.services.inventory

import org.springframework.data.util.Pair

interface InventoryService {
    /**
     * Get a single KeyValuePair by a Key for a given ServiceInstance.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param key key of the KeyValuePair.
     * @return KeyValuePair
     */
    Pair<String, String> get(String serviceInstanceGuid, String key)

    /**
     * Get a single KeyValuePair by a given Key for a given ServiceInstance - will return a default value if no KeyValuePair
     * matching this configuration matches.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param key Key of the KeyValuePair.
     * @param defaultValue Default Value to be returned
     * @return KeyValuePair
     */
    Pair<String, String> get(String serviceInstanceGuid, String key, String defaultValue)

    /**
     * Get all KeyValuePairs for a given Key for a given ServiceInstance (this is especially useful if the key is not unique).
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param key Key of the KeyValuePair.
     * @return List of KeyValuePair
     */
    List<Pair<String, String>> getAll(String serviceInstanceGuid, String key)

    /**
     * Get all KeyValuePairs for a given ServiceInstance.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @return List of all KeyValuePair for this ServiceInstance.
     */
    List<Pair<String, String>> get(String serviceInstanceGuid)

    /**
     * Sets a KeyValuePair for a given ServiceInstance replacing previous instances if necessary.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @return List of all KeyValuePair for this ServiceInstance.
     */
    List<Pair<String, String>> set(String serviceInstanceGuid, Pair<String, String> data)

    /**
     * Replaces all KeyValuePair for a given ServiceInstance with a new List of KeyValuePair; all KeyValuePairs not in
     * the data List will be deleted.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param data List of all KeyValuePair to be set for this ServiceInstance.
     * @return List of all KeyValuePair for this ServiceInstance.
     */
    List<Pair<String, String>> replace(String serviceInstanceGuid, List<Pair<String, String>> data)

    /**
     * Adds a List of KeyValuePairs for a given ServiceInstance; not touching previously existing KeyValuePairs.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param data List of KeyValuePair to be appended for this ServiceInstance.
     * @return List of all KeyValuePair for this ServiceInstance.
     */
    List<Pair<String, String>> append(String serviceInstanceGuid, List<Pair<String, String>> data)

    /**
     * Deletes all KeyValuePairs for a given ServiceInstance for a given Key.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param key Key of the KeyValuePair.
     * @return List of all KeyValuePair for this ServiceInstance.
     */
    List<Pair<String, String>> delete(String serviceInstanceGuid, String key)

    /**
     * Replaces or Creates all KeyValuePairs with a given Key for a given ServiceInstance; Not touching any KeyValuePairs
     * not matching the given Key. This is especially useful for working with not unique keys.
     *
     * @param serviceInstanceGuid Id of th ServiceInstance.
     * @param key Key of the KeyValuePair.
     * @param values
     * @return List of all KeyValuePair for this ServiceInstance.
     */
    List<Pair<String, String>> replaceByKey(String serviceInstanceGuid, String key, String[] values)
}
