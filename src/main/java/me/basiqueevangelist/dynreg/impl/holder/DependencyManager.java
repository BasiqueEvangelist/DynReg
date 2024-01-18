package me.basiqueevangelist.dynreg.impl.holder;

import me.basiqueevangelist.dynreg.api.RegistryModification;
import me.basiqueevangelist.dynreg.api.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.impl.DynReg;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

                    RegistryModification.remove(info.registry(), info.id());
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
            .computeIfAbsent(registryOf(dependency), DependencyManager::new)
            .DEPENDENCY_TO_DEPENDENTS
            .computeIfAbsent(dependency.value(), unused -> new HashSet<>())
            .add(new RegistrationInfo(registryOf(dependent), dependent.registryKey().getValue()));

        MANAGERS
            .computeIfAbsent(registryOf(dependent), DependencyManager::new)
            .DEPENDENT_TO_DEPENDENCIES
            .computeIfAbsent(dependent.value(), unused -> new HashSet<>())
            .add(dependency);

        if (DynReg.DEBUG) {
            LOGGER.info("Created dependency {} -> {}", dependency.registryKey().getValue(), dependent.registryKey().getValue());
        }
    }

    public static void removeDependency(RegistryEntry.Reference<?> dependent, RegistryEntry.Reference<?> dependency) {
        var depManager = MANAGERS.get(registryOf(dependency));

        if (depManager != null) {
            var dependentsList = depManager.DEPENDENCY_TO_DEPENDENTS.get(dependency.value());

            if (dependentsList != null) {
                dependentsList.remove(new RegistrationInfo(registryOf(dependent), dependent.registryKey().getValue()));

                if (dependentsList.size() == 0)
                    depManager.DEPENDENCY_TO_DEPENDENTS.remove(dependency.value());
            }
        }

        var dependentManager = MANAGERS.get(registryOf(dependent));

        if (dependentManager != null) {
            var depsList = dependentManager.DEPENDENT_TO_DEPENDENCIES.get(dependent.value());

            if (depsList != null) {
                depsList.remove(dependency);

                if (depsList.size() == 0)
                    dependentManager.DEPENDENT_TO_DEPENDENCIES.remove(dependent.value());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> registryOf(RegistryEntry.Reference<T> key) {
        return (Registry<T>) Registries.REGISTRIES.get(key.registryKey().getRegistry());
    }

    private record RegistrationInfo(Registry<?> registry, Identifier id) { }
}
