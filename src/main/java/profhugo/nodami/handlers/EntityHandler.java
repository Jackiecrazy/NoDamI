package profhugo.nodami.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import profhugo.nodami.config.NodamiConfig;

public class EntityHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void attackStart(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.getLastDamageSource() != null&&NodamiConfig.differenceMode) {
            DamageSource lastDS = entity.getLastDamageSource();
            DamageSource thisDS = event.getSource();
            if(lastDS!=null&&thisDS!=null)
            if (lastDS.getTrueSource() == thisDS.getTrueSource() || lastDS.getImmediateSource() == thisDS.getImmediateSource()){
            	//same source
            	if(lastDS.damageType.equals(thisDS.damageType)){
            		//same damage type
					if(entity.hurtTime>0){
						event.setCanceled(true);
					}
				}
			}
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityHurt(LivingHurtEvent event) {
        if (!event.isCanceled()) {
            EntityLivingBase entity = event.getEntityLiving();
            if (entity.getEntityWorld().isRemote) {
                return;
            }
            DamageSource source = event.getSource();
            if (NodamiConfig.debugMode && entity instanceof EntityPlayer) {
                String message = String.format("Type of damage received: %s\nAmount: %.3f\nTrue Source (mob id): %s\n",
                        source.getDamageType(), event.getAmount(), source.getTrueSource() == null ? "null"
                                : EntityList.getKey(source.getTrueSource().getClass()).toString());
                ((EntityPlayer) entity).sendMessage(new TextComponentString(message));
            }
            if (entity instanceof EntityPlayer) {
                if (NodamiConfig.excludePlayers)
                    return;
                ItemStack is = entity.getHeldItemMainhand();
                for (String s : NodamiConfig.itemSrcWhitelistMain) {
                    if (is.getItem().getUnlocalizedName(is).equals(s)) return;
                }
                is = entity.getHeldItemOffhand();
                for (String s : NodamiConfig.itemSrcWhitelistOff) {
                    if (is.getItem().getUnlocalizedName(is).equals(s)) return;
                }
            }
            if (NodamiConfig.excludeAllMobs && !(entity instanceof EntityPlayer)) {
                return;
            }
            for (String id : NodamiConfig.dmgReceiveExcludedEntities) {
                ResourceLocation loc = EntityList.getKey(entity.getClass());
                if (loc == null)
                    break;
                int starIndex = id.indexOf('*');
                if (starIndex != -1) {
                    if (loc.toString().contains(id.substring(0, starIndex))) {
                        return;
                    }
                } else if (loc.toString().equals(id)) {
                    return;
                }
            }
            // May have more DoTs missing in this list
            // Not anymore (/s)
            for (String dmgType : NodamiConfig.damageSrcWhitelist) {
                if (source.getDamageType().equals(dmgType)) {
                    return;
                }
            }

            // Mobs that do damage on collusion but have no attack timer
            for (String id : NodamiConfig.attackExcludedEntities) {
                if (source.getTrueSource() == null)
                    break;
                ResourceLocation loc = EntityList.getKey(source.getTrueSource().getClass());
                if (loc == null)
                    break;
                int starIndex = id.indexOf('*');
                if (starIndex != -1) {
                    if (loc.toString().contains(id.substring(0, starIndex))) {
                        return;
                    }
                } else if (loc.toString().equals(id)) {
                    return;
                }

            }
            entity.hurtResistantTime = NodamiConfig.iFrameInterval;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerAttack(AttackEntityEvent event) {
        if (!event.isCanceled()) {
            EntityPlayer player = event.getEntityPlayer();
            if (player.getEntityWorld().isRemote) {
                return;
            }
            float str = player.getCooledAttackStrength(0);
            if (str <= NodamiConfig.attackCancelThreshold) {
                event.setCanceled(true);
                return;
            }
            if (str <= NodamiConfig.knockbackCancelThreshold) {
                // Don't worry, it's only magic
                player.hurtTime = -1;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKnockback(LivingKnockBackEvent event) {
        if (!event.isCanceled()) {
            Entity attacker = event.getAttacker();
            if (attacker != null && !attacker.getEntityWorld().isRemote) {
                // IT'S ONLY MAGIC
                if (attacker instanceof EntityPlayer && ((EntityPlayer) attacker).hurtTime == -1) {
                    event.setCanceled(true);
                    ((EntityPlayer) attacker).hurtTime = 0;
                }
            }
        }
    }

    private class DamageEntry {
        int aid, pid, time;
        String ds;

        private DamageEntry(DamageSource ds, int attackerID, int proxyID, int timestamp) {
            aid = attackerID;
            pid = proxyID;
            time = timestamp;
            this.ds = ds.damageType;
        }
    }

}
