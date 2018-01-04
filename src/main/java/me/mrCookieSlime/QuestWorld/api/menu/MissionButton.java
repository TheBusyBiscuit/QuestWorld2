package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static me.mrCookieSlime.QuestWorld.util.json.Prop.*;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.SinglePrompt;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.manager.PlayerStatus;
import me.mrCookieSlime.QuestWorld.manager.ProgressTracker;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.PlayerTools;
import me.mrCookieSlime.QuestWorld.util.Text;
import me.mrCookieSlime.QuestWorld.util.json.JsonBlob;
import me.mrCookieSlime.QuestWorld.util.json.Prop;

public class MissionButton {
	public static MenuData item(IMissionState changes) {
		return simpleButton(changes,
				new ItemBuilder(changes.getItem()).wrapLore(
						"",
						"&e> Click to set the Item to the one in your hand").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					ItemStack hand = p.getInventory().getItemInMainHand();
					if(hand != null && hand.getType() != Material.AIR)
						changes.setItem(hand.clone());
				}
		);
	}

	public static MenuData amount(IMissionState changes) {
		return amount(changes, 16);
	}
	
	public static MenuData amount(IMissionState changes, int groupSize) {
		return simpleButton(changes,
				new ItemBuilder(Material.REDSTONE).wrapText(
						"&7Amount: &b" + changes.getAmount(),
						"",
						"&rLeft Click: &e+1",
						"&rRight Click: &e-1",
						"&rShift + Left Click: &e+"+groupSize,
						"&rShift + Right Click: &e-"+groupSize).get(),
				event -> {
					int amt = clickNumber(changes.getAmount(), groupSize, event);
					changes.setAmount(Math.max(amt, 1));
				}
		);
	}
	
	public static MenuData entity(IMissionState changes) {
		EntityType entity = changes.getEntity();
		return new MenuData(
				EntityTools.getEntityDisplay(entity).wrapText(
						"&7Entity Type: &r" + EntityTools.nameOf(entity),
						"",
						"&e> Click to change the Entity").get(),
				event -> {
					QBDialogue.openQuestMissionEntityEditor((Player)event.getWhoClicked(), changes);
				}
		);
	}
	
	public static MenuData location(IMissionState changes) {
		return simpleButton(changes,
				new ItemBuilder(changes.getDisplayItem()).wrapText(
						Text.stringOf(changes.getLocation(), changes.getCustomInt()),
						"",
						"&e> Click to change the Location to your current Position").get(),
				event -> {
					changes.setLocation(event.getWhoClicked().getLocation().getBlock().getLocation());
				}
		);
	}
	
	public static MenuData entityName(IMissionState changes) {
		return new MenuData(
				new ItemBuilder(Material.NAME_TAG).wrapText(
						"&r" + changes.getCustomString(),
						"",
						"&e> Click to change the Name").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();

					p.closeInventory();
					PlayerTools.promptInput(p, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.KILLMISSION_NAME_EDIT),
							(c,s) -> {
								changes.setCustomString(Text.colorize(s));
								if(changes.apply()) {
									PlayerTools.sendTranslation(p, true, Translation.KILLMISSION_NAME_SET);
								}
								QuestBook.openQuestMissionEditor(p, changes);
								return true;
							}
					));
				}
		);
	}
	
	public static MenuData missionName(IMissionState changes) {
		return new MenuData(
				new ItemBuilder(Material.NAME_TAG).wrapText(
						changes.getText(),
						"",
						"&rLeft Click: Edit Mission Name",
						"&rRight Click: Reset Mission Name").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
			
					if(event.getClick().isRightClick()) {
						changes.setDisplayName("");
						apply(event, changes);
					}
					else {
						p.closeInventory();
						PlayerTools.promptInput(p, new SinglePrompt(
								PlayerTools.makeTranslation(true, Translation.MISSION_NAME_EDIT),
								(c,s) -> {
									changes.setDisplayName(s);
									if(changes.apply()) {
										PlayerTools.sendTranslation(p, true, Translation.MISSION_NAME_SET);
										QuestBook.openQuestMissionEditor(p, changes.getSource());
									}
									return true;
								}
						));
					}
				}
		);
	}

	public static MenuData timeframe(IMissionState changes) {
		return simpleButton(changes,
				new ItemBuilder(Material.WATCH).wrapText(
						"&7Complete Mission within: &b" + Text.timeFromNum(changes.getTimeframe()),
						"",
						"&rLeft Click: &e+1m",
						"&rRight Click: &e-1m",
						"&rShift + Left Click: &e+1h",
						"&rShift + Right Click: &e-1h").get(),
				event -> {
					int amt = clickNumber(changes.getTimeframe(), 60, event);
					changes.setTimeframe(Math.max(amt, 0));
				}
		);
	}
	
	public static MenuData deathReset(IMissionState changes) {
		String icon = changes.getDeathReset() ? "&2&l\u2714": "&4&l\u2718";
		return simpleButton(changes,
				new ItemBuilder(Material.SKULL_ITEM).wrapText(
						"&7Resets on Death: " + icon,
						"",
						"&e> Click to change whether this Mission's Progress resets when a Player dies").get(),
				event -> {
					changes.setDeathReset(!changes.getDeathReset());
				}
		);
	}
	
	public static MenuData spawnersAllowed(IMissionState changes) {
		String icon = changes.getSpawnerSupport() ? "&2&l\u2714": "&4&l\u2718";
		return simpleButton(changes,
				new ItemBuilder(Material.MOB_SPAWNER).wrapText(
						"&7Allow Mobs from Spawners: " + icon,
						"",
						"&e> Click to change whether this Mission will also count Mobs which were spawned by a Mob Spawner").get(),
				event -> {
					changes.setSpawnerSupport(!changes.getSpawnerSupport());
				}
		);
	}
	
	private static void dialogueThing(Player p, IMission mission) {
		
		List<String> dialogue = mission.getDialogue();
		
		p.sendMessage(Text.colorize("&7&m----------------------------"));
		int size = dialogue.size();
		for(int i = 0; i < size; ++i) {
			String s = dialogue.get(i);
			int index = i;
			Prop remove = FUSE(
					HOVER_TEXT("Click to remove " + (s.startsWith("/") ? "command" : "dialogue")),
					CLICK_RUN(p, () -> {
						dialogue.remove(index);
						
						IMissionState state = mission.getState();
						state.setDialogue(dialogue);
						if(state.apply())
							dialogueThing(p, mission);
					}));
			
			PlayerTools.tellraw(p,
					new JsonBlob("X ", DARK_RED, remove)
					.addLegacy(Text.colorize(s), WHITE, remove).toString());
		}
		
		Prop add = FUSE(
				HOVER_TEXT("Click to add dialogue", GRAY),
				CLICK_RUN(p, () -> {
					
					PlayerTools.promptInputOrCommand(p, new SinglePrompt(
							PlayerTools.makeTranslation(true, Translation.MISSION_DIALOG_ADD),
							null,
							(c,s) -> {
								if (s.equalsIgnoreCase("exit()") || s.equalsIgnoreCase("/exit")) {
									IMissionState state = mission.getState();
									state.setDialogue(dialogue);
									if(state.apply()) {
										String filename = ProgressTracker.dialogueFile(state.getSource()).getName();
										PlayerTools.sendTranslation(p, true, Translation.MISSION_DIALOG_SET, filename);
										dialogueThing(p, mission);
									}
									return true;
								}
								dialogue.add(s);
								
								Translation translator = s.startsWith("/") ?
										Translation.MISSION_COMMAND_ADDED : Translation.MISSION_DIALOG_ADDED;
								
								SinglePrompt.setNextDisplay(c, PlayerTools.makeTranslation(true, translator, s));
								QuestWorld.getSounds().DIALOG_ADD.playTo(p);
								
								return false;
							}
					));
					
					p.sendMessage(Text.colorize("&7Usable Variables: @p (Username)"));
					
				}));
		
		PlayerTools.tellraw(p, new JsonBlob("+ ", DARK_GREEN, add)
				.add("Add more dialogue", GRAY, add).toString());
		
		Prop back = FUSE(
				HOVER_TEXT("Open mission editor", GRAY),
				CLICK_RUN(p, () -> QuestBook.openQuestMissionEditor(p, mission) ));
		
		PlayerTools.tellraw(p, new JsonBlob("< ", BLUE, back)
				.add("Return to mission editor", GRAY, back).toString());
		
		p.sendMessage(Text.colorize("&7&m----------------------------"));
	}
	
	public static MenuData dialogue(IMissionState changes) {
		return new MenuData(
				new ItemBuilder(Material.PAPER).wrapText(
						"&rDialogue",
						"",
						"&rLeft Click: Edit the Dialogue",
						"&rRight Click: Dialogue Preview").get(),
				event -> {
					Player p = (Player)event.getWhoClicked();
					
					if (event.getClick().isRightClick()) {
						if (changes.getDialogue().isEmpty())
							p.sendMessage(Text.colorize("&4No Dialogue found!"));
						else
							PlayerStatus.sendDialogue(p.getUniqueId(), changes, changes.getDialogue().iterator());
						return;
					}

					dialogueThing(p, changes.getSource());
					p.closeInventory();
				}
		);
	}
	
	public static void apply(InventoryClickEvent event, IMissionState changes) {
		if(changes.apply()) {
			QuestBook.openQuestMissionEditor((Player) event.getWhoClicked(), changes.getSource());
		}
	}

	public static MenuData simpleButton(IMissionState changes, ItemStack item, Consumer<InventoryClickEvent> action)  {
		return new MenuData(item, action.andThen(event -> apply(event, changes)));
	}
	
	public static int clickNumber(int initial, int groupSize, InventoryClickEvent event) {
		switch(event.getClick()) {
		case RIGHT: initial -= 1; break;
		case SHIFT_RIGHT: initial -= groupSize; break;
		case LEFT: initial += 1; break;
		case SHIFT_LEFT: initial += groupSize; break;
		case NUMBER_KEY: initial = initial * 10 + event.getHotbarButton() + 1; break;
		case DROP: initial /= 10; break;
		default: break;
		}
		
		return initial;
	}
}
