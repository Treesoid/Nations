package io.github.treesoid.nations.abilities.util;

import io.github.treesoid.nations.Nations;
import io.github.treesoid.nations.helper.ServerPlayerHelper;
import io.github.treesoid.nations.network.s2c.SyncAbilityListPacket;
import io.github.treesoid.nations.server.NationsServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerAbilityList {
    public final UUID player;
    public final List<PlayerAbility> abilities = new LinkedList<>();
    private final MinecraftServer server;
    public PlayerAbility selectedAbility = null;

    public PlayerAbilityList(UUID player, MinecraftServer server) {
        this.server = server;
        this.player = player;
    }

    public boolean addAbility(Ability ability) {
        if (!ServerPlayerHelper.playerOnline(player, server)) return false;
        PlayerEntity playerEntity = ServerPlayerHelper.getPlayer(player, server);
        if (!hasAbility(ability) && ability.canObtain(playerEntity)) {
            PlayerAbility newAbility = new PlayerAbility(playerEntity, ability);
            abilities.add(newAbility);
            NationsServer.DATABASE_HANDLER.addPlayerAbility(newAbility, player);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeAbility(Ability ability) {
        return abilities.removeIf(ability1 -> {
            if (ability.matches(ability1)) {
                NationsServer.DATABASE_HANDLER.removePlayerAbility(ability1.ability, player);
                return true;
            }
            return false;
        });
    }

    public void tick() {
        for (PlayerAbility ability : abilities) {
            ability.tickCooldown();
        }
    }

    public boolean hasAbility(Ability ability) {
        return abilities.stream().anyMatch(ability::matches);
    }

    public boolean hasAbilitySelected() {
        return selectedAbility != null;
    }

    public boolean favouriteAbility(Ability ability) {
        return favouriteAbility(ability, true);
    }

    public boolean favouriteAbility(Ability ability, boolean favourite) {
        AtomicBoolean output = new AtomicBoolean(false);
        this.abilities.stream()
                .filter(ability::matches)
                .filter(ability1 -> ability1.isFavourite() != favourite)
                .forEach(ability1 -> {
                    ability1.setFavourite(favourite);
                    NationsServer.DATABASE_HANDLER.updatePlayerAbility(ability1, player);
                    output.set(true);
                });
        return output.get();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean selectAbility(Ability ability) {
        if (ability == null) {
            this.selectedAbility = null;
            NationsServer.DATABASE_HANDLER.setSelectedAbility(player, null);
            NationsServer.DATABASE_HANDLER.getOrCreatePlayerData(player, server).setSelectedAbility(null);
            return true;
        }
        Optional<PlayerAbility> optionalPlayerAbility = abilities.stream().filter(ability::matches).findFirst();
        if (optionalPlayerAbility.isEmpty()) return false;
        this.selectedAbility = optionalPlayerAbility.get();
        NationsServer.DATABASE_HANDLER.setSelectedAbility(player, selectedAbility.ability);
        NationsServer.DATABASE_HANDLER.getOrCreatePlayerData(player, server).setSelectedAbility(selectedAbility.ability);
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean selectAbility(Identifier ability) {
        return selectAbility(Nations.ABILITY_REGISTRY.get(ability));
    }

    public void useSelectedAbility() {
        if (hasAbilitySelected()) {
            selectedAbility.use();
        }
    }

    public boolean isOnline() {
        return ServerPlayerHelper.playerOnline(player, server);
    }

    public void updateAllAbilities() {
        for (PlayerAbility ability : abilities) {
            NationsServer.DATABASE_HANDLER.updatePlayerAbility(ability, player);
        }
    }

    public NbtCompound serialize() {
        NbtCompound compound = new NbtCompound();
        NbtList list = new NbtList();
        for (PlayerAbility ability : abilities) {
            list.add(ability.serialize());
        }
        compound.put("abilities", list);
        if (hasAbilitySelected()) {
            compound.putString("selectedAbility", selectedAbility.ability.identifier.toString());
        }
        return compound;
    }

    public void sync() {
        if (isOnline()) {
            SyncAbilityListPacket.send(ServerPlayerHelper.getPlayer(player, server), serialize());
        }
    }
}
