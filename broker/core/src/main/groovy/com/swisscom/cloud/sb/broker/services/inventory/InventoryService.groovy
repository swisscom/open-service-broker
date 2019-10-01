package com.swisscom.cloud.sb.broker.services.inventory

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import org.springframework.data.util.Pair

interface InventoryService {
    /**
     * @return First {@link Pair} with given key; If no {@link Pair} is found an {@link IllegalStateException} is thrown.
     */
    Pair<String, String> get(String serviceInstanceGuid, String key)

    /**
     * Get all {@link Pair} for a given Key for a given {@link ServiceInstance} (this is especially useful if the key is not unique).
     *
     * @return All {@link Pair} for a given key (including duplicates) for a given {@link ServiceInstance}.
     */
    List<Pair<String, String>> getAll(String serviceInstanceGuid, String key)

    /**
     * Get the first {@link Pair} by a given Key for a given {@link ServiceInstance} - will return a {@link Pair} with the
     * default value if no {@link Pair} matching this configuration exists.
     */
    Pair<String, String> get(String serviceInstanceGuid, String key, String defaultValue)

    /**
     * @deprecated Use {@link InventoryService#getAll()} instead.
     */
    @Deprecated
    List<Pair<String, String>> get(String serviceInstanceGuid)

    /**
     * @return All {@link Pair}s of the  {@link ServiceInstance}.
     */
    List<Pair<String, String>> getAll(String serviceInstanceGuid)

    /**
     * Create or Updates a {@link Pair} for a given {@link ServiceInstance}; Will only update the first {@link Pair} if
     * multiple duplicated keys exist.
     *
     * @return All {@link Pair}s of the {@link ServiceInstance}.
     */
    List<Pair<String, String>> set(String serviceInstanceGuid, Pair<String, String> data)

    /**
     * Replaces all {@link Pair} for a given {@link ServiceInstance} with a new List of {@link Pair}; all {@link Pair} not in
     * the data list will be deleted.
     *
     * @return All {@link Pair}s of the {@link ServiceInstance}.
     */
    List<Pair<String, String>> replace(String serviceInstanceGuid, List<Pair<String, String>> data)

    /**
     * Adds a list of {@link Pair}s for a given {@link ServiceInstance}; not touching previously existing {@link Pair}s.
     *
     * @return All {@link Pair}s of the {@link ServiceInstance}.
     */
    List<Pair<String, String>> append(String serviceInstanceGuid, List<Pair<String, String>> data)

    /**
     * @return All {@link Pair}s of the {@link ServiceInstance}.
     */
    List<Pair<String, String>> delete(String serviceInstanceGuid, String key)

    /**
     * Replaces or Creates all {@link Pair}s with a given Key for a given {@link ServiceInstance}; Not touching any {@link Pair}s
     * not matching the given Key. This is especially useful for working with not unique keys.
     *
     * @return All {@link Pair}s of the {@link ServiceInstance}.
     */
    List<Pair<String, String>> createOrReplaceByKey(String serviceInstanceGuid, String key, String[] values)
}
