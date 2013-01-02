package me.max;

import java.awt.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.*;

public class wl implements Listener{
	public w plugin;
	public wl(w p){
		plugin = p;
	}
	public static Map<MCTree, ArrayList<Block>> isaTree = new HashMap<MCTree, ArrayList<Block>>();

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if(e.isCancelled()) return;
		Block b = e.getBlock();
		Player p = e.getPlayer();
		if(b.getType() == Material.LOG){
			if(p.getGameMode() == GameMode.CREATIVE) return;
			
			MCTree tree = new MCTree(b);
			Block breakme = tree.removeTrunkTop();
			if(breakme == null) breakme = b;
			if(breakme.getLocation().equals(b.getLocation()) && 
			(breakme.getRelative(0, -1, 0).getType() == Material.DIRT || breakme.getRelative(0, -1, 0).getType() == Material.GRASS) &&
			isInTree(breakme)) {

				// Is bottom
				e.setCancelled(true);
				byte data = b.getData();
				dropBlock(b,p,tree);
				b.setType(Material.SAPLING);
				b.setData(data);
				if(isTree(tree)) isaTree.remove(tree);
				return;
			}
			if(isInTree(breakme)){
				FakeBreak(p,breakme);
				e.setCancelled(true);
				return;
			}else if(breakme.getRelative(0, 1, 0).getType() == Material.LEAVES){
				isaTree.put(tree, null);
				
				final BlockBreakEvent newBlockBreak = new BlockBreakEvent(b,p);

				FakeBreak(p,breakme);
				e.setCancelled(true);
				
				 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		             public void run() {
		 				Bukkit.getServer().getPluginManager().callEvent(newBlockBreak);

		             }
		         }, 2);

			}
			
			
			
			

			
			
		}
	}
	public void FakeBreak(final Player p, final Block b){
		ItemStack i = p.getItemInHand();
		short dur = (short) (i.getDurability() + 1);
		i.setDurability((short) dur);
		///////////////////////
		
		
		
		int type_id = b.getData();
		int thing = 0;
		
		switch(type_id) {
		case 0:
			thing = 0;
			break;
		case 1:
			thing = 1;
			break;
		case 2:
			thing = 2;
			break;
		}
		final int data = thing;
		
		 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             public void run() {
                b.setData((byte) data);
                dropBlock(b,p,new MCTree(b));
         		b.setType(Material.AIR);
             }
         }, 3);
		
		
		
			
	}
	public void dropBlock(Block block,Player p, MCTree tree){
		ncpe(p);
		Material material = block.getType();
		int amount = 1;
		byte data = block.getData();
		short damage = 0;
		ItemStack drop = new ItemStack(material, amount, damage, data);
		if(drop.getType() == Material.AIR) return;
		block.getWorld().dropItemNaturally(p.getEyeLocation(), drop);
	}
	public boolean isTree(MCTree t){
		
		return isaTree.containsKey(t);
	}
	public boolean isInTree(Block b){
		boolean inTree = false;
		for(MCTree t : isaTree.keySet()){
			if(t.isInTrunk(b)){
				inTree = true;
			}
		}
		return inTree;
	}
	public void ncpe(final Player p){
		NCPExemptionManager.exemptPermanently(p, CheckType.BLOCKBREAK);
		 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             public void run() {
               NCPExemptionManager.unexempt(p);
             }
         }, 10);
	}
}
