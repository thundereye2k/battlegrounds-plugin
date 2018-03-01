package com.matsg.battlegrounds.item;

import com.matsg.battlegrounds.api.game.GamePlayer;
import com.matsg.battlegrounds.api.item.Attachment;
import com.matsg.battlegrounds.api.item.Gun;
import com.matsg.battlegrounds.api.item.Projectile;
import com.matsg.battlegrounds.api.item.ReloadType;
import com.matsg.battlegrounds.api.util.Hitbox;
import com.matsg.battlegrounds.api.util.Sound;
import com.matsg.battlegrounds.util.BattleSound;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleGun extends BattleFireArm implements Gun {

    private boolean scoped;
    private Bullet bullet;
    private FireMode fireMode;
    private int burstRounds, fireRate, hits, scopeZoom;
    private List<Attachment> attachments;

    public BattleGun(String name, String description, ItemStack itemStack, short durability,
                     int magazine, int ammo, int maxAmmo,  int cooldown, int reloadDuration, double accuracy,
                     Bullet bullet, FireMode fireMode, ReloadType reloadType, FireArmType fireArmType, Sound[] reloadSound, Sound[] shootSound) {
        super(name, description, itemStack, durability, magazine, ammo, maxAmmo, cooldown, reloadDuration, accuracy, reloadType, fireArmType, reloadSound, shootSound);
        this.attachments = new ArrayList<>();
        this.bullet = bullet;
        this.fireMode = fireMode;
        this.scopeZoom = 10;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public int getBurstRounds() {
        return 0;
    }

    public int getFireRate() {
        return 0;
    }

    public Projectile getProjectile() {
        return bullet;
    }

    protected String[] getLore() {
        return new String[0];
    }

    private List<Location> getSpreadDirections(Location direction, int amount) {
        if (amount <= 0) {
            return Collections.EMPTY_LIST;
        }
        List<Location> list = new ArrayList<>();
        double spreadDensity = 50.0; // Spread density constant
        for (int i = 0; i < amount; i ++) {
            double degrees = i / amount * 360;

            Location spread = direction.clone();
            float pitch = (float) (spread.getPitch() + degrees / spreadDensity);
            float yaw = (float) (spread.getYaw() + (degrees - 180) / spreadDensity);

            spread.setPitch(pitch);
            spread.setYaw(yaw);

            list.add(spread);
        }
        return list;
    }

    private void inflictDamage(Location location, double range) {
        GamePlayer[] players = game.getPlayerManager().getNearbyPlayers(location, range);
        if (players.length > 0) {
            GamePlayer gamePlayer = players[0];
            if (gamePlayer == null || gamePlayer == this.gamePlayer || gamePlayer.getPlayer().isDead()) {
                return;
            }
            Hitbox hitbox = Hitbox.getHitbox(gamePlayer.getLocation().getY(), location.getY());
            gamePlayer.getLocation().getWorld().playEffect(location, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            gamePlayer.getPlayer().damage(0.0); // Create a fake damage animation
            game.getPlayerManager().damagePlayer(gamePlayer, bullet.getDamage(hitbox, gamePlayer.getLocation().distanceSquared(this.gamePlayer.getLocation())));
            hits ++;
        }
    }

    public void onLeftClick() {
        if (fireArmType.hasScope() && scoped) {
            setScoped(false);
            return;
        }
        if (reloading || shooting || ammo <= 0 || magazine >= magazineSize) {
            return;
        }
        reload(reloadDuration);
    }

    public void onRightClick() {
        if (reloading || shooting) {
            return;
        }
        if (fireArmType.hasScope() && !scoped) {
            setScoped(true);
            return;
        }
        if (magazine <= 0) {
            if (ammo > 0) {
                reload(reloadDuration); // Reload if the magazine is empty
            }
            return;
        }
        shoot();
    }

    public void setScoped(boolean scoped) {
        this.scoped = scoped;

        Player player = getGamePlayer().getPlayer();

        if (scoped) {
            for (Sound sound : BattleSound.GUN_SCOPE) {
                sound.play(game, player.getLocation());
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000, -scopeZoom)); //Zoom effect
            player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN, 1));
        } else {
            BattleSound.GUN_SCOPE[0].play(game, player.getLocation(), (float) 0.75);
            BattleSound.GUN_SCOPE[1].play(game, player.getLocation(), (float) 1.5);

            player.getInventory().setHelmet(null);
            player.removePotionEffect(PotionEffectType.SPEED); //Restore zoom effect
        }
    }

    public void shoot() {
        shooting = true;
        fireMode.shoot(this, fireRate, burstRounds);
    }

    public void shootProjectile() {
        shootProjectile(getShootingDirection(gamePlayer.getPlayer().getEyeLocation().subtract(0, 0.25, 0)), fireArmType.getProjectileAmount());
    }

    private void shootProjectile(Location direction) {
        double distance = 0.5, range = 0.1; // Multiplier and range constant

        do {
            Vector vector = direction.getDirection();
            direction.add(vector.multiply(distance));

            displayParticle(direction, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            inflictDamage(direction, range);

            Block block = direction.getBlock();

            if (!blocks.contains(block.getType())) {
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
                return;
            }

            direction.subtract(vector); // Restore the location
            distance += 0.5;
        } while (distance < bullet.getLongRange() && hits < fireArmType.getMaxHits()); // If the projectile distance exceeds the long range, stop the loop

        hits = 0;
    }

    private void shootProjectile(Location direction, int amount) {
        if (amount <= 0) {
            return;
        }
        for (Location spreadDirection : getSpreadDirections(direction, amount - 1)) {
            shootProjectile(spreadDirection);
        }
        shootProjectile(direction);
    }
}