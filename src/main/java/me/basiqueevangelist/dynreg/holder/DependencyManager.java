package me.basiqueevangelist.dynreg.holder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public final class DependencyManager {
    private static final Map<Registry<?>, DependencyManager> MANAGERS = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/DependencyManager");

    private final WeakHashMap<Object, Collection<RegistrationInfo>> DEPENDENCY_TO_DEPENDENTS = new WeakHashMap<>();
    private final WeakHashMap<Object, Collection<RegistryEntry.Reference<?>>> DEPENDENT_TO_DEPENDENCIES = new WeakHashMap<>();

    private DependencyManager(Registry<?> registry) {
        RegistryEntryDeletedCallback.event(registry).register((rawId, entry) -> {
            Collection<RegistrationInfo> dependents = DEPENDENCY_TO_DEPENDENTS.remove(entry.value());

            if (dependents != null) {
                for (RegistrationInfo info : dependents) {
                    if (!info.registry.containsId(info.id)) {
                        LOGGER.warn("Dependent {} wasn't registered by the time {} was deleted", info.id, entry.registryKey().getValue());
                        continue;
                    }

                    if (DynReg.DEBUG) {
                        LOGGER.info("Removing {}, as it is a dependent of {}", info.id, entry.registryKey().getValue());
                    }

                    RegistryUtils.remove(info.registry(), info.id());
                }
            }

            Collection<RegistryEntry.Reference<?>> dependencies = DEPENDENT_TO_DEPENDENCIES.remove(entry.value());

            if (dependencies != null) {
                for (var depEntry : dependencies) {
                    removeDependency(entry, depEntry);
                }
            }
        });
    }

    public static void addDependency(RegistryEntry.Reference<?> dependent, RegistryEntry.Reference<?> dependency) {
        MANAGERS
            .computeIfAbsent(dependency.registry, DependencyManager::new)
            .DEPENDENCY_TO_DEPENDENTS
            .computeIfAbsent(dependency.value(), unused -> new HashSet<>())
            .add(new RegistrationInfo(dependent.registry, dependent.registryKey().getValue()));

        MANAGERS
            .computeIfAbsent(dependent.registry, DependencyManager::new)
            .DEPENDENT_TO_DEPENDENCIES
            .computeIfAbsent(dependent.value(), unused -> new HashSet<>())
            .add(dependency);

        if (DynReg.DEBUG) {
            LOGGER.info("Created dependency {} -> {}", dependency.registryKey().getValue(), dependent.registryKey().getValue());
        }
    }

    public static void removeDependency(RegistryEntry.Reference<?> dependent, RegistryEntry.Reference<?> dependency) {
        var depManager = MANAGERS.get(dependency.registry);

        if (depManager != null) {
            var dependentsList = depManager.DEPENDENCY_TO_DEPENDENTS.get(dependency.value());

            if (dependentsList != null) {
                dependentsList.remove(new RegistrationInfo(dependent.registry, dependent.registryKey().getValue()));

                if (dependentsList.size() == 0)
                    depManager.DEPENDENCY_TO_DEPENDENTS.remove(dependency.value());
            }
        }

        var dependentManager = MANAGERS.get(dependent.registry);

        if (dependentManager != null) {
            var depsList = dependentManager.DEPENDENT_TO_DEPENDENCIES.get(dependent.value());

            if (depsList != null) {
                depsList.remove(dependency);

                if (depsList.size() == 0)
                    dependentManager.DEPENDENT_TO_DEPENDENCIES.remove(dependent.value());
            }
        }
    }

    private record RegistrationInfo(Registry<?> registry, Identifier id) { }
}
