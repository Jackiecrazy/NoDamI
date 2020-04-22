package profhugo.nodami.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import profhugo.nodami.NoDamI;

@Config(modid = NoDamI.MODID)
@Config.LangKey("NoDamI.config")
public class NodamiConfig {

    @Config.Comment("How many ticks of i-frames does an entity get when damaged, from 0 (default), to 2^31-1 (nothing can take damage)")
    @Config.RangeInt(min = 0)
    public static int iFrameInterval = 0;
    @Config.Comment("Are players excluded from this mod (if true, players will always get 10 ticks of i-frames on being damaged")
    public static boolean excludePlayers = false;
    @Config.Comment("Are all mobs excluded from this mod (if true, mobs will always get 10 ticks of i-farmes on being damaged")
    public static boolean excludeAllMobs = false;
    @Config.Comment("If true, turns on feature which sends a message when a player receives damage, containing information such as the name of the source and the quantity. Use this to find the name of the source you need to whitelist, or the id of the mob you want to exclude.")
    public static boolean debugMode = false;
    @Config.Comment("If true, an attack on an entity will give i-frames against the attacker entity and the damage source. Experimental.")
    public static boolean differenceMode = false;
    @Config.Comment("How weak a player's attack can be before it gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, players cannot attack), or -0.1 (disables this feature)")
    @Config.RangeDouble(min = -0.1f, max = 1)
    public static float attackCancelThreshold = 0.1f;
    @Config.Comment("How weak a player's attack can be before the knockback gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, no knockback), or -0.1 (disables this feature)")
    @Config.RangeDouble(min = -0.1f, max = 1)
    public static float knockbackCancelThreshold = 0.75f;
    @Config.Comment("List of entities that need to give i-frames on attacking")
    public static String[] attackExcludedEntities = {"minecraft:slime", "tconstruct:blueslime", "thaumcraft:thaumslime", "grimoireofgaia:*"};
    @Config.Comment("List of entities that need to receive i-frames on receiving attacks or relies on iFrames")
    public static String[] dmgReceiveExcludedEntities = {};
    @Config.Comment("List of damage sources that need to give i-frames on doing damage (ex: lava).")
    public static String[] damageSrcWhitelist = {"inFire", "lava", "cactus", "lightningBolt", "inWall", "hotFloor"};
    @Config.Comment("List of items that need to give i-frames when held by the player on the mainhand.")
    public static String[] itemSrcWhitelistMain = {};
    @Config.Comment("List of items that need to give i-frames when held by the player on the offhand.")
    public static String[] itemSrcWhitelistOff = {};

    @Mod.EventBusSubscriber(modid = NoDamI.MODID)
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(NoDamI.MODID)) {
                ConfigManager.sync(NoDamI.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
