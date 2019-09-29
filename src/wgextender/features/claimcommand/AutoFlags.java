/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package wgextender.features.claimcommand;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
//import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.commands.region.RegionCommands;
//import com.sk89q.worldguard.bukkit.commands.region.RegionCommands;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import wgextender.Config;
import wgextender.utils.WGRegionUtils;

public class AutoFlags {

	protected static boolean hasRegion(final World world, final String regionname) {
		return getRegion(world, regionname) != null;
	}

	protected static ProtectedRegion getRegion(final World world, final String regionname) {
		final RegionManager rm = WGRegionUtils.getRegionManager(world);
		if (rm == null) {
			return null;
		}
		return rm.getRegion(regionname);
	}

	protected static void setFlagsForRegion(final World world, final Config config, final String regionname,
			org.bukkit.Location location) {
		final ProtectedRegion rg = getRegion(world, regionname);
		if (rg != null) {
			for (Entry<Flag<?>, String> entry : config.autoflags.entrySet()) {
				try {
					setFlag(world, rg, entry.getKey(), entry.getValue(), location);
				} catch (CommandException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static final RegionCommands regionCommands = new RegionCommands(WorldGuard.getInstance()); // no fawe
	// protected static final RegionCommands regionCommands = new
	// RegionCommands(WorldGuardPlugin.inst()); //fawe
	protected static final Set<Character> flagCommandValueFlags = getFlagCommandValueFlags();

	public static <T> void setFlag(World world, ProtectedRegion region, Flag<?> flag, String value,
			org.bukkit.Location location) throws CommandException {
		FakeConsoleComandSender fakeCommandSender = new FakeConsoleComandSender();
		CommandContext ccontext = new CommandContext(
				String.format("flag %s -w %s %s %s", region.getId(), world.getName(), flag.getName(), value),
				flagCommandValueFlags);
		fakeCommandSender.setWorld(world, location);
		regionCommands.flag(ccontext, fakeCommandSender);
	}

	protected static Set<Character> getFlagCommandValueFlags() {
		try {
			Method method = RegionCommands.class.getMethod("flag", CommandContext.class, Actor.class); // no fawe
			// Method method = RegionCommands.class.getMethod("flag", CommandContext.class,
			// CommandSender.class); //fawe
			Command annotation = method.getAnnotation(Command.class);
			char[] flags = annotation.flags().toCharArray();
			Set<Character> valueFlags = new HashSet<>();
			for (int i = 0; i < flags.length; ++i) {
				if ((flags.length > (i + 1)) && (flags[i + 1] == ':')) {
					valueFlags.add(flags[i]);
					++i;
				}
			}
			return valueFlags;
		} catch (Throwable t) {
			t.printStackTrace();
			return Collections.emptySet();
		}
	}

	private static class FakeConsoleComandSender implements Actor, LocalPlayer {
		private World world;
		private org.bukkit.Location vector;

		@Override
		public String getName() {
			return Bukkit.getConsoleSender().getName();
		}

		public void setWorld(World w, org.bukkit.Location location) {
			world = w;
			vector = location;
		}

		@Override
		public boolean hasPermission(String arg0) {
			return true;
		}

		@Override
		public UUID getUniqueId() {
			return UUID.randomUUID();
		}

		@Override
		public SessionKey getSessionKey() {
			return null;
		}

		@Override
		public void checkPermission(String arg0) throws AuthorizationException {
		}

		@Override
		public String[] getGroups() {
			return null;
		}

		@Override
		public boolean canDestroyBedrock() {
			return false;
		}

		@Override
		public void dispatchCUIEvent(CUIEvent arg0) {
		}

		@Override
		public boolean isPlayer() {
			return true;
		}

		@Override
		public File openFileOpenDialog(String[] arg0) {
			return null;
		}

		@Override
		public File openFileSaveDialog(String[] arg0) {
			return null;
		}

		@Override
		public void print(String arg0) {
		}

		@Override
		public void print(Component arg0) {
		}

		@Override
		public void printDebug(String arg0) {
		}

		@Override
		public void printError(String arg0) {
		}

		@Override
		public void printRaw(String arg0) {
		}

		@Override
		public boolean ascendLevel() {
			return false;
		}

		@Override
		public boolean ascendToCeiling(int arg0) {
			return false;
		}

		@Override
		public boolean ascendToCeiling(int arg0, boolean arg1) {
			return false;
		}

		@Override
		public boolean ascendUpwards(int arg0) {
			return false;
		}

		@Override
		public boolean ascendUpwards(int arg0, boolean arg1) {
			return false;
		}

		@Override
		public boolean descendLevel() {
			return false;
		}

		@Override
		public void findFreePosition() {
		}

		@Override
		public void findFreePosition(Location arg0) {
		}

		@Override
		public void floatAt(int arg0, int arg1, int arg2, boolean arg3) {
		}

		@Override
		public Location getBlockIn() {
			return null;
		}

		@Override
		public BaseBlock getBlockInHand(HandSide arg0) throws WorldEditException {
			return null;
		}

		@Override
		public Location getBlockOn() {
			return null;
		}

		@Override
		public Location getBlockTrace(int arg0) {
			return null;
		}

		@Override
		public Location getBlockTrace(int arg0, boolean arg1) {
			return null;
		}

		@Override
		public Location getBlockTrace(int arg0, boolean arg1, Mask arg2) {
			return null;
		}

		@Override
		public Location getBlockTraceFace(int arg0, boolean arg1) {
			return null;
		}

		@Override
		public Location getBlockTraceFace(int arg0, boolean arg1, Mask arg2) {
			return null;
		}

		@Override
		public Direction getCardinalDirection() {
			return null;
		}

		@Override
		public Direction getCardinalDirection(int arg0) {
			return null;
		}

		@Override
		public GameMode getGameMode() {
			return null;
		}

		@Override
		public BlockBag getInventoryBlockBag() {
			return null;
		}

		@Override
		public BaseItemStack getItemInHand(HandSide arg0) {
			return null;
		}

		@Override
		public Location getSolidBlockTrace(int arg0) {
			return null;
		}

		@Override
		public com.sk89q.worldedit.world.World getWorld() {
			return BukkitAdapter.adapt(world);
		}

		@Override
		public void giveItem(BaseItemStack arg0) {
		}

		@Override
		public boolean isHoldingPickAxe() {
			return false;
		}

		@Override
		public boolean passThroughForwardWall(int arg0) {
			return false;
		}

		@Override
		public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 arg0, B arg1) {
		}

		@Override
		public void setGameMode(GameMode arg0) {
		}

		@Override
		public void setOnGround(Location arg0) {
		}

		@Override
		public void setPosition(Vector3 arg0) {
		}

		@Override
		public void setPosition(Vector3 arg0, float arg1, float arg2) {
		}

		@Override
		public Extent getExtent() {
			return null;
		}

		@Override
		public Location getLocation() {
			return new com.sk89q.worldedit.util.Location(BukkitAdapter.adapt(world), vector.getX(), vector.getY(),
					vector.getZ(), vector.getYaw(), vector.getPitch());
		}

		@Override
		public BaseEntity getState() {
			return null;
		}

		@Override
		public boolean remove() {
			return false;
		}

		@Override
		public boolean setLocation(Location arg0) {
			return false;
		}

		@Override
		public <T> T getFacet(Class<? extends T> arg0) {
			return null;
		}

		@Override
		public void ban(String arg0) {
		}

		@Override
		public float getExhaustion() {
			return 0;
		}

		@Override
		public int getFireTicks() {
			return 0;
		}

		@Override
		public double getFoodLevel() {
			return 0;
		}

		@Override
		public double getHealth() {
			return 0;
		}

		@Override
		public double getMaxHealth() {
			return 0;
		}

		@Override
		public long getPlayerTimeOffset() {
			return 0;
		}

		@Override
		public WeatherType getPlayerWeather() {
			return null;
		}

		@Override
		public double getSaturation() {
			return 0;
		}

		@Override
		public boolean hasGroup(String arg0) {
			return false;
		}

		@Override
		public boolean isPlayerTimeRelative() {
			return false;
		}

		@Override
		public void kick(String arg0) {
		}

		@Override
		public void resetFallDistance() {
		}

		@Override
		public void resetPlayerTime() {
		}

		@Override
		public void resetPlayerWeather() {
		}

		@Override
		public void sendTitle(String arg0, String arg1) {
		}

		@Override
		public void setCompassTarget(Location arg0) {
		}

		@Override
		public void setExhaustion(float arg0) {
		}

		@Override
		public void setFireTicks(int arg0) {
		}

		@Override
		public void setFoodLevel(double arg0) {
		}

		@Override
		public void setHealth(double arg0) {
		}

		@Override
		public void setPlayerTime(long arg0, boolean arg1) {
		}

		@Override
		public void setPlayerWeather(WeatherType arg0) {
		}

		@Override
		public void setSaturation(double arg0) {
		}
	}
}
