package com.TealNerd.SkyNet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import org.lwjgl.input.Keyboard;
 
@Mod(modid="skynet", name="SkyNet", version="1.4.0")
public class SkyNet {
   
    static Minecraft mc = Minecraft.getMinecraft();
    static boolean isEnabled = true;
    public static KeyBinding toggle;
    static List<String> previousPlayerList = new ArrayList<String>();
    static boolean justLoggedIn = true;
   
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)  {
        MinecraftForge.EVENT_BUS.register(this);
        toggle = new KeyBinding("Toggle SkyNet", Keyboard.KEY_I, "SkyNet");
        ClientRegistry.registerKeyBinding(toggle);    
    }
   
    public static String filterChatColors(String s) {
        return TextFormatting.getTextWithoutFormattingCodes(s);
    }
   
    public static void onPlayerJoin(String player) {
        showMessage(player, "joined", TextFormatting.DARK_GREEN);
    }

    public static void onPlayerLeave(String player) {
        showMessage(player, "left", TextFormatting.GRAY);
    }

    private static void showMessage(String player, String action, TextFormatting actionColor) {
        mc.player.sendMessage(new TextComponentString("[SkyNet] ")
                .setStyle(new Style().setColor(TextFormatting.DARK_AQUA))
                .appendSibling(new TextComponentString(String.format("%s %s the game", player, action))
                        .setStyle(new Style().setColor(actionColor))));
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            TextFormatting color = event.getMessage().getSiblings().get(0).getStyle().getColor();
            if (color == TextFormatting.YELLOW) {
                String msgText = event.getMessage().getUnformattedText();
                if (msgText.endsWith(" joined the game") || msgText.endsWith(" left the game")) {
                    event.setCanceled(true);
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
        } catch (NullPointerException ignored) {
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            if(SkyNet.isEnabled) {
                if(mc.world != null) {
                    ArrayList<String> playerList = new ArrayList<String>();
                    Collection<NetworkPlayerInfo> players = mc.getConnection().getPlayerInfoMap();
                    for(Object o : players) {
                        if((o instanceof NetworkPlayerInfo)) {
                            NetworkPlayerInfo info = (NetworkPlayerInfo)o;
                            playerList.add(SkyNet.filterChatColors(info.getGameProfile().getName()));
                        }
                    }
                    ArrayList<String> temp = (ArrayList<String>)playerList.clone();
                    playerList.removeAll(SkyNet.previousPlayerList);
                    SkyNet.previousPlayerList.removeAll(temp);
                    if (!justLoggedIn) {
                        for(String player : SkyNet.previousPlayerList) {
                            SkyNet.onPlayerLeave(player);
                        }
                        for(String player : playerList) {
                            SkyNet.onPlayerJoin(player);
                        }
                    }
                    SkyNet.previousPlayerList = temp;
                    justLoggedIn = false;
                }
                if(mc.world == null) {
                    justLoggedIn = true;
                }
            }
        }
    }
   
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(SkyNet.toggle.isPressed()){
            if(!SkyNet.isEnabled){
            mc.player.sendMessage(new TextComponentString(TextFormatting.DARK_AQUA + "[SkyNet] "+ TextFormatting.GRAY + "SkyNet Enabled"));
            SkyNet.isEnabled = true;
            }else if(SkyNet.isEnabled){
            mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + "[SkyNet] "+ TextFormatting.GRAY + "SkyNet Disabled"));
            SkyNet.isEnabled = false;
            }
        }              
    }
}
