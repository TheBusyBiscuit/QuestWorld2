package me.mrCookieSlime.QuestWorld.quest;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.event.MissionChangeEvent;
import me.mrCookieSlime.QuestWorld.util.BitFlag;
import me.mrCookieSlime.QuestWorld.util.BitFlag.BitString;

class MissionChange extends Mission {
	private long changeBits = 0;
	private Mission origin;
	
	public MissionChange(Mission copy) {
		super(copy.serialize());
		setUnique(copy.getUnique());
		origin = copy;
	}
	
	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}
	
	public List<Member> getChanges() {
		return BitFlag.getFlags(Member.values(), changeBits);
	}
	
	public Mission getSource() {
		return origin;
	}
	
	@Override
	public boolean apply() {
		if(sendEvent()) {
			copyTo(origin);
			changeBits = 0;
			return true;
		}
		return false;
	}

	@Override
	public boolean discard() {
		if(changeBits != 0) {
			copy(origin);
			changeBits = 0;
			return true;
		}
		return false;
	}
	
	private boolean sendEvent() {
		if(changeBits == 0)
			return false;
		
		return CancellableEvent.send(new MissionChangeEvent(this));
	}
	
	// Modify "setX" methods
	@Override
	public void setItem(ItemStack item) {
		super.setItem(item);
		changeBits |= BitFlag.getBits(Member.ITEM);
	}

	@Override
	public void setEntity(EntityType entity) {
		super.setEntity(entity);
		changeBits |= BitFlag.getBits(Member.ENTITY);
	}

	@Override
	public void setCustomString(String customString) {
		super.setCustomString(customString);
		changeBits |= BitFlag.getBits(Member.NAME);
	}

	@Override
	public void setType(MissionType type) {
		// TODO RIP migrateFrom
		if(true) {
			loadDefaults();
			changeBits |= BitString.ALL;
		}
		super.setType(type);
		changeBits |= BitFlag.getBits(Member.TYPE);
	}

	@Override
	public void setAmount(int amount) {
		super.setAmount(amount);
		changeBits |= BitFlag.getBits(Member.AMOUNT);
	}
	
	@Override
	public void setLocation(Location loc) {
		super.setLocation(loc);
		changeBits |= BitFlag.getBits(Member.LOCATION);
	}

	@Override
	public void setLocation(Player p) {
		super.setLocation(p);
		changeBits |= BitFlag.getBits(Member.LOCATION);
	}
	
	@Override
	public void setDisplayName(String name) {
		super.setDisplayName(name);
		changeBits |= BitFlag.getBits(Member.NAME);
	}

	@Override
	public void setTimeframe(int timeframe) {
		super.setTimeframe(timeframe);
		changeBits |= BitFlag.getBits(Member.TIMEFRAME);
	}

	@Override
	public void setDeathReset(boolean deathReset) {
		super.setDeathReset(deathReset);
		changeBits |= BitFlag.getBits(Member.DEATH_RESET);
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
		changeBits |= BitFlag.getBits(Member.LORE);
	}

	@Override
	public void setCustomInt(int val) {
		super.setCustomInt(val);
		changeBits |= BitFlag.getBits(Member.CUSTOM_INT);
	}

	@Override
	public void setSpawnerSupport(boolean acceptsSpawners) {
		super.setSpawnerSupport(acceptsSpawners);
		changeBits |= BitFlag.getBits(Member.SPAWNERS_ALLOWED);
	}
	
	// TODO hacky hacky
	@Override
	public void setupDialogue(Player p) {
		origin.setupDialogue(p);
	}
	
	@Override
	public MissionChange getState() {
		return this;
	}
}
