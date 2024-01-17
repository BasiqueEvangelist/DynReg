package me.basiqueevangelist.dynreg.testmod.desc;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.api.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.testmod.DynRegTest;
import me.basiqueevangelist.dynreg.api.ser.LazyEntryRef;
import me.basiqueevangelist.dynreg.util.NamedEntries;
import me.basiqueevangelist.dynreg.api.ser.SimpleReaders;
import me.basiqueevangelist.dynreg.api.ser.SimpleSerializers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class SimpleEntityEntry implements RegistrationEntry {
    public static final Identifier ID = DynRegTest.id("simple_entity");

    private final Identifier id;
    private final SpawnGroup spawnGroup;
    private final Set<LazyEntryRef<Block>> canSpawnInside;
    private final boolean saveable;
    private final boolean summonable;
    private final boolean fireImmune;
    private final boolean spawnableFarFromPlayer;
    private final int maxTrackingRange;
    private final int trackingTickInterval;
    private final EntityDimensions dimensions;
    private EntityType<SimpleEntity> entityType;

    public SimpleEntityEntry(Identifier id, JsonObject obj) {
        this.id = id;
        this.spawnGroup = NamedEntries.SPAWN_GROUPS.get(JsonHelper.getString(obj, "spawn_group"));
        this.canSpawnInside = new HashSet<>();
        this.saveable = JsonHelper.getBoolean(obj, "summonable", true);
        this.summonable = JsonHelper.getBoolean(obj, "summonable", true);
        this.fireImmune = JsonHelper.getBoolean(obj, "fire_immune", false);
        this.spawnableFarFromPlayer = JsonHelper.getBoolean(
            obj,
            "spawnable_far_from_player",
            spawnGroup == SpawnGroup.CREATURE || spawnGroup == SpawnGroup.MISC
        );
        this.maxTrackingRange = JsonHelper.getInt(obj, "max_tracking_range", 5);
        this.trackingTickInterval = JsonHelper.getInt(obj, "tracking_tick_interval", 3);
        this.dimensions = SimpleReaders.readEntityDimensions(obj.get("dimensions"));

        for (JsonElement el : JsonHelper.getArray(obj, "can_spawn_inside", new JsonArray())) {
            canSpawnInside.add(
                new LazyEntryRef<>(Registries.BLOCK, new Identifier(JsonHelper.asString(el, "<array element>"))));
        }
    }

    public SimpleEntityEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.spawnGroup = NamedEntries.SPAWN_GROUPS.get(buf.readString());
        this.canSpawnInside = buf.readCollection(HashSet::new, buf1 -> LazyEntryRef.read(buf1, Registries.BLOCK));
        this.saveable = buf.readBoolean();
        this.summonable = buf.readBoolean();
        this.fireImmune = buf.readBoolean();
        this.spawnableFarFromPlayer = buf.readBoolean();
        this.maxTrackingRange = buf.readVarInt();
        this.trackingTickInterval = buf.readVarInt();
        this.dimensions = SimpleSerializers.readEntityDimensions(buf);
    }

    @Override
    public void scan(EntryScanContext ctx) {
        for (var entry : canSpawnInside) {
            ctx.dependency(entry);
        }
    }

    @Override
    public void register(EntryRegisterContext ctx) {
        entityType = new EntityType<>(SimpleEntity::new, spawnGroup, saveable, summonable, fireImmune,
            spawnableFarFromPlayer, canSpawnInside.stream().map(LazyEntryRef::get).collect(ImmutableSet.toImmutableSet()),
            dimensions, maxTrackingRange, trackingTickInterval, FeatureSet.empty());

        ctx.register(Registries.ENTITY_TYPE, id, entityType);
        FabricDefaultAttributeRegistry.register(entityType,
            MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void registerClient() {
        EntityRendererRegistry.register(entityType, EmptyEntityRenderer::new);
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(NamedEntries.SPAWN_GROUPS.inverse().get(spawnGroup));
        buf.writeCollection(canSpawnInside, (buf1, ref) -> ref.write(buf1));
        buf.writeBoolean(saveable);
        buf.writeBoolean(summonable);
        buf.writeBoolean(fireImmune);
        buf.writeBoolean(spawnableFarFromPlayer);
        buf.writeVarInt(maxTrackingRange);
        buf.writeVarInt(trackingTickInterval);
        SimpleSerializers.writeEntityDimensions(buf, dimensions);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long hash() {
        int result = id.hashCode();
        result = 31 * result + spawnGroup.hashCode();
        result = 31 * result + canSpawnInside.hashCode();
        result = 31 * result + (saveable ? 1 : 0);
        result = 31 * result + (summonable ? 1 : 0);
        result = 31 * result + (fireImmune ? 1 : 0);
        result = 31 * result + (spawnableFarFromPlayer ? 1 : 0);
        result = 31 * result + maxTrackingRange;
        result = 31 * result + trackingTickInterval;
        result = 31 * result + dimensions.hashCode();
        return result;
    }

    private static class SimpleEntity extends PathAwareEntity {
        protected SimpleEntity(EntityType<SimpleEntity> type, World world) {
            super(type, world);
        }

        @Override
        protected void initGoals() {
            this.goalSelector.add(0, new SwimGoal(this));
            this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0));
            this.goalSelector.add(3, new TemptGoal(this, 1.25, Ingredient.ofItems(Items.BOWL), false));
            this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
            this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
            this.goalSelector.add(7, new LookAroundGoal(this));
        }
    }
}
