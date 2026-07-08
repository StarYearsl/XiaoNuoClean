package com.xiaonuoclean.cleanup;

import com.xiaonuoclean.config.CleanConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.List;

public class ItemCleanupService {
    public int clean(MinecraftServer server, CleanConfig config) {
        CleanupWhitelist whitelist = new CleanupWhitelist(config.whitelist());
        List<CleanupTarget> targets = new ArrayList<>();

        for (var level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
                    String itemId = itemId(itemEntity);
                    int count = itemEntity.getItem().getCount();
                    targets.add(new CleanupTarget(itemId, count, itemEntity::discard));
                }
            }
        }

        return new CleanupTargetCleaner().clean(targets, whitelist);
    }

    private String itemId(ItemEntity itemEntity) {
        return BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem()).toString();
    }
}
