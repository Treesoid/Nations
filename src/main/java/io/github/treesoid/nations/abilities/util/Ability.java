package io.github.treesoid.nations.abilities.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class Ability {
    public final Identifier identifier;

    public Ability(Identifier identifier) {
        this.identifier = identifier;
    }

    public abstract boolean canObtain(PlayerEntity  player);

    public void onUse(PlayerAbility ability) {
        System.out.println("ability " + identifier + " used, cooldown: " + ability.getCooldown());
        if (canUse(ability)) {
            onTrigger(ability);
            ability.setCooldown(getMaxCooldown());
        }
    }

    public abstract int getMaxCooldown();

    public boolean canUse(PlayerAbility ability) {
        return !ability.hasCooldown();
    }

    @Environment(EnvType.CLIENT)
    public abstract Identifier getIcon();

    public boolean matches(Ability ability) {
        return identifier.equals(ability.identifier);
    }

    public boolean matches(PlayerAbility ability) {
        return identifier.equals(ability.ability.identifier);
    }

    protected abstract void onTrigger(@NotNull PlayerAbility ability);
}
