package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.DBCore;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.MySQLCore;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.SQLiteCore;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.GriefBlock;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Unbreakable;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Vec;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author phaed
 */
public final class StorageManager
{
	/**
     *
     */
	private DBCore core;
	private final PreciousStones plugin;
	private final Map<Vec, Field> pending = new HashMap<Vec, Field>();
	private final Map<Unbreakable, Boolean> pendingUb = new HashMap<Unbreakable, Boolean>();
	private final Map<String, Boolean> pendingPlayers = new HashMap<String, Boolean>();
	private final Set<Field> pendingGrief = new HashSet<Field>();
	private final List<SnitchEntry> pendingSnitchEntries = new LinkedList<SnitchEntry>();

	/**
     *
     */
	public StorageManager()
		{
			plugin = PreciousStones.getInstance();

			initiateDB();
			loadWorldData();
			saverScheduler();
		}

	private void initiateDB()
		{
			if (plugin.getSettingsManager().isUseMysql())
				{
					core = new MySQLCore(plugin.getSettingsManager().getHost(), plugin.getSettingsManager().getPort(), plugin.getSettingsManager().getDatabase(), plugin.getSettingsManager().getUsername(), plugin.getSettingsManager().getPassword());

					if (core.checkConnection())
						{
							PreciousStones.log("MySQL Connection successful");

							if (!core.existsTable("pstone_cuboids"))
								{
									PreciousStones.log("Creating table: pstone_cuboids");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_cuboids` (  `id` bigint(20) NOT NULL auto_increment, `parent` bigint(20) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `minx` int(11) default NULL,  `maxx` int(11) default NULL,  `miny` int(11) default NULL,  `maxy` int(11) default NULL,  `minz` int(11) default NULL,  `maxz` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_cuboid_fields_1`  (`x`,`y`,`z`,`world`));");
								}

							if (!core.existsTable("pstone_fields"))
								{
									PreciousStones.log("Creating table: pstone_fields");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_fields_1` (`x`,`y`,`z`,`world`));");
								}

							if (!core.existsTable("pstone_unbreakables"))
								{
									PreciousStones.log("Creating table: pstone_unbreakables");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` bigint(20) NOT NULL auto_increment,  `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(25) default NULL,  `owner` varchar(16) NOT NULL,  `type_id` int(11) default NULL,  PRIMARY KEY  (`id`),  UNIQUE KEY `uq_pstone_unbreakables_1` (`x`,`y`,`z`,`world`));");
								}

							if (!core.existsTable("pstone_grief_undo"))
								{
									PreciousStones.log("Creating table: pstone_grief_undo");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` bigint(20) NOT NULL auto_increment,  `date_griefed` datetime NOT NULL, `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(25) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL, PRIMARY KEY  (`id`));");
								}

							if (!core.existsTable("pstone_players"))
								{
									PreciousStones.log("Creating table: pstone_players");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_players` ( `id` bigint(20), `player_name` varchar(16) NOT NULL, `last_seen` bigint(20) default NULL, flags TEXT NOT NULL, PRIMARY KEY  (`player_name`));");
									touchAllPlayers();
								}

							if (!core.existsTable("pstone_snitches"))
								{
									PreciousStones.log("Creating table: pstone_snitches");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_snitches` ( `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL, `name` varchar(16) NOT NULL, `reason` varchar(20) default NULL, `details` varchar(50) default NULL, `count` int(11) default NULL, PRIMARY KEY  (`x`, `y`, `z`, `world`, `name`, `reason`, `details`));");
								}
						} else
						{
							PreciousStones.log("MySQL Connection failed");
						}
				} else
				{
					core = new SQLiteCore("PreciousStones", plugin.getDataFolder().getPath());

					if (core.checkConnection())
						{
							PreciousStones.log("SQLite Connection successful");

							if (!core.existsTable("pstone_cuboids"))
								{
									PreciousStones.log("Creating table: pstone_cuboids");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_cuboids` (  `id` INTEGER PRIMARY KEY,  `parent` bigint(20) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `minx` int(11) default NULL,  `maxx` int(11) default NULL,  `miny` int(11) default NULL,  `maxy` int(11) default NULL,  `minz` int(11) default NULL,  `maxz` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, UNIQUE (`x`,`y`,`z`,`world`));");
								}

							if (!core.existsTable("pstone_fields"))
								{
									PreciousStones.log("Creating table: pstone_fields");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_fields` (  `id` INTEGER PRIMARY KEY, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL,  `radius` int(11) default NULL,  `height` int(11) default NULL,  `velocity` float default NULL,  `type_id` int(11) default NULL,  `owner` varchar(16) NOT NULL,  `name` varchar(50) NOT NULL,  `packed_allowed` text NOT NULL, `last_used` bigint(20) Default NULL, `flags` TEXT NOT NULL, UNIQUE (`x`,`y`,`z`,`world`));");
								}

							if (!core.existsTable("pstone_unbreakables"))
								{
									PreciousStones.log("Creating table: pstone_unbreakables");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_unbreakables` (  `id` INTEGER PRIMARY KEY, `x` int(11) default NULL,  `y` int(11) default NULL,  `z` int(11) default NULL,  `world` varchar(25) default NULL,  `owner` varchar(16) NOT NULL,  `type_id` int(11) default NULL, UNIQUE (`x`,`y`,`z`,`world`));");
								}

							if (!core.existsTable("pstone_grief_undo"))
								{
									PreciousStones.log("Creating table: pstone_grief_undo");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_grief_undo` (  `id` INTEGER PRIMARY KEY,  `date_griefed` datetime NOT NULL, `field_x` int(11) default NULL,  `field_y` int(11) default NULL, `field_z` int(11) default NULL, `world` varchar(25) NOT NULL, `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL, `type_id` int(11) NOT NULL,  `data` TINYINT NOT NULL,  `sign_text` varchar(75) NOT NULL);");
								}

							if (!core.existsTable("pstone_players"))
								{
									PreciousStones.log("Creating table: pstone_players");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_players` ( `id` bigint(20), `player_name` varchar(16) NOT NULL, `last_seen` bigint(20) default NULL, flags TEXT NOT NULL, PRIMARY KEY (`player_name`));");
									touchAllPlayers();
								}

							if (!core.existsTable("pstone_snitches"))
								{
									PreciousStones.log("Creating table: pstone_snitches");

									core.execute("CREATE TABLE IF NOT EXISTS `pstone_snitches` ( `id` bigint(20), `x` int(11) default NULL,  `y` int(11) default NULL, `z` int(11) default NULL,  `world` varchar(25) default NULL, `name` varchar(16) NOT NULL, `reason` varchar(20) default NULL, `details` varchar(50) default NULL, `count` int(11) default NULL, PRIMARY KEY  (`x`, `y`, `z`, `world`, `name`, `reason`, `details`));");
								}
						} else
						{
							PreciousStones.log("SQLite Connection failed");
						}
				}

			// fixes();
		}

	@SuppressWarnings("unused")
	private void fixes()
		{
			if (!core.existsColumn("flags", "pstone_players"))
				{
					PreciousStones.log("Adding flags column to pstone_players table");

					core.execute("alter table pstone_players add column flags TEXT");
				}

			if (!core.existsColumn("flags", "pstone_fields"))
				{
					PreciousStones.log("Adding flags column to pstone_fields table");

					core.execute("alter table pstone_fields add column flags TEXT");
				}

			if (!core.existsColumn("flags", "pstone_cuboids"))
				{
					PreciousStones.log("Adding flags column to pstone_cuboids table");

					core.execute("alter table pstone_cuboids add column flags TEXT");
				}
		}

	/**
	 * Closes DB connection
	 */
	public void closeConnection()
		{
			core.close();
		}

	/**
	 * Load pstones for any world that is loaded
	 */
	public void loadWorldData()
		{
			plugin.getForceFieldManager().clearChunkLists();
			plugin.getUnbreakableManager().clearChunkLists();

			final List<World> worlds = plugin.getServer().getWorlds();

			for (final World world : worlds)
				{
					loadWorldFields(world.getName());
					loadWorldUnbreakables(world.getName());
				}

			extractPlayers();
		}

	/**
	 * Loads all fields for a specific world into memory
	 * 
	 * @param world
	 *            the world to load
	 */
	public void loadWorldFields(final String world)
		{
			List<Field> fields;

			synchronized (this)
				{
					fields = getFields(world);
					fields.addAll(getCuboidFields(world));
				}

			if (fields != null)
				{
					for (final Field field : fields)
						{
							// add to collection

							plugin.getForceFieldManager().addToCollection(field);

							if (field.hasFlag(FieldFlag.GRIEF_REVERT) && field.getRevertSecs() > 0)
								{
									plugin.getGriefUndoManager().register(field);
								}
						}
				}

			if (fields.size() > 0)
				{
					PreciousStones.log("({0}) fields: {1}", world, fields.size());
				}
		}

	/**
	 * Loads all unbreakables for a specific world into memory
	 * 
	 * @param world
	 */
	public void loadWorldUnbreakables(final String world)
		{
			List<Unbreakable> unbreakables;

			synchronized (this)
				{
					unbreakables = getUnbreakables(world);
				}

			if (unbreakables != null)
				{
					for (final Unbreakable ub : unbreakables)
						{
							plugin.getUnbreakableManager().addToCollection(ub);
						}
				}

			if (unbreakables.size() > 0)
				{
					PreciousStones.log("({0}) unbreakables: {1}", world, unbreakables.size());
				}
		}

	/**
	 * Puts the field up for future storage
	 * 
	 * @param field
	 */
	public void offerField(final Field field)
		{
			synchronized (pending)
				{
					pending.put(field.toVec(), field);
				}
		}

	/**
	 * Puts the unbreakable up for future storage
	 * 
	 * @param ub
	 * @param insert
	 */
	public void offerUnbreakable(final Unbreakable ub, final boolean insert)
		{
			synchronized (pendingUb)
				{
					pendingUb.put(ub, insert);
				}
		}

	/**
	 * Puts the field up for grief reversion
	 * 
	 * @param field
	 */
	public void offerGrief(final Field field)
		{
			synchronized (pendingGrief)
				{
					pendingGrief.add(field);
				}
		}

	/**
	 * Puts the player up for future storage
	 * 
	 * @param playerName
	 * @param update
	 */
	public void offerPlayer(final String playerName, final boolean update)
		{
			synchronized (pendingPlayers)
				{
					pendingPlayers.put(playerName, update);
				}
		}

	/**
	 * Puts the snitch list up for future storage
	 * 
	 * @param se
	 */
	public void offerSnitchEntry(final SnitchEntry se)
		{
			synchronized (pendingSnitchEntries)
				{
					pendingSnitchEntries.add(se);
				}
		}

	/**
	 * Retrieves all fields belonging to a world from the database
	 * 
	 * @param worldName
	 * @return
	 */
	public List<Field> getFields(final String worldName)
		{
			final List<Field> out = new ArrayList<Field>();
			int purged = 0;

			final String query = "SELECT pstone_fields.id as id, x, y, z, radius, height, type_id, velocity, world, owner, name, packed_allowed, last_used, flags FROM pstone_fields WHERE world = '" + Helper.escapeQuotes(worldName) + "';";

			final ResultSet res = core.select(query);

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final long id = res.getLong("id");
											final int x = res.getInt("x");
											final int y = res.getInt("y");
											final int z = res.getInt("z");
											final int radius = res.getInt("radius");
											final int height = res.getInt("height");
											final int type_id = res.getInt("type_id");
											final float velocity = res.getFloat("velocity");
											final String world = res.getString("world");
											final String owner = res.getString("owner");
											final String name = res.getString("name");
											final String flags = res.getString("flags");
											final String packed_allowed = res.getString("packed_allowed");
											final long last_used = res.getLong("last_used");

											final Field field = new Field(x, y, z, radius, height, velocity, world, type_id, owner, name, last_used);
											field.setPackedAllowed(packed_allowed);
											field.setId(id);

											final FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);

											if (field.getAgeInDays() > plugin.getSettingsManager().getPurgeSnitchAfterDays())
												{
													if (fs != null && fs.hasDefaultFlag(FieldFlag.SNITCH))
														{
															deleteSnitchEntries(field);
															field.markForDeletion();
															offerField(field);
															purged++;
															continue;
														}
												}

											if (fs != null)
												{
													field.setSettings(fs);
													field.setFlags(flags);
													out.add(field);

													final PlayerData data = plugin.getPlayerManager().getPlayerData(owner);
													data.incrementFieldCount(type_id);
												}
										} catch (final Exception ex)
										{
											PreciousStones.getLogger().info(ex.getMessage());
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			if (purged > 0)
				{
					PreciousStones.log("({0}) fields purged: {1}", worldName, purged);
				}
			return out;
		}

	/**
	 * Retrieves all of the cuboid fields belonging to a world from the database
	 * 
	 * @param worldName
	 * @return
	 */
	public Collection<Field> getCuboidFields(final String worldName)
		{
			final HashMap<Long, Field> out = new HashMap<Long, Field>();
			int purged = 0;

			String query = "SELECT pstone_cuboids.id as id, x, y, z, minx, miny, minz, maxx, maxy, maxz, type_id, velocity, world, owner, name, packed_allowed, last_used, flags  FROM  pstone_cuboids WHERE pstone_cuboids.parent = 0 AND world = '" + Helper.escapeQuotes(worldName) + "';";

			ResultSet res = core.select(query);

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final long id = res.getLong("id");
											final int x = res.getInt("x");
											final int y = res.getInt("y");
											final int z = res.getInt("z");
											final int minx = res.getInt("minx");
											final int miny = res.getInt("miny");
											final int minz = res.getInt("minz");
											final int maxx = res.getInt("maxx");
											final int maxy = res.getInt("maxy");
											final int maxz = res.getInt("maxz");
											final int type_id = res.getInt("type_id");
											final float velocity = res.getFloat("velocity");
											final String world = res.getString("world");
											final String owner = res.getString("owner");
											final String name = res.getString("name");
											final String flags = res.getString("flags");
											final String packed_allowed = res.getString("packed_allowed");
											final long last_used = res.getLong("last_used");

											final Field field = new Field(x, y, z, minx, miny, minz, maxx, maxy, maxz, velocity, world, type_id, owner, name, last_used);
											field.setPackedAllowed(packed_allowed);
											field.setId(id);

											final FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);

											if (field.getAgeInDays() > plugin.getSettingsManager().getPurgeSnitchAfterDays())
												{
													if (fs != null && fs.hasDefaultFlag(FieldFlag.SNITCH))
														{
															deleteSnitchEntries(field);
															field.markForDeletion();
															offerField(field);
															purged++;
															continue;
														}
												}

											if (fs != null)
												{
													field.setSettings(fs);
													field.setFlags(flags);
													out.put(id, field);

													final PlayerData data = plugin.getPlayerManager().getPlayerData(owner);
													data.incrementFieldCount(type_id);
												}
										} catch (final Exception ex)
										{
											ex.printStackTrace();
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			query = "SELECT pstone_cuboids.id as id, parent, x, y, z, minx, miny, minz, maxx, maxy, maxz, type_id, velocity, world, owner, name, packed_allowed, last_used, flags FROM  pstone_cuboids WHERE pstone_cuboids.parent > 0 AND world = '" + Helper.escapeQuotes(worldName) + "';";

			res = core.select(query);

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final long id = res.getLong("id");
											final long parent = res.getLong("parent");
											final int x = res.getInt("x");
											final int y = res.getInt("y");
											final int z = res.getInt("z");
											final int minx = res.getInt("minx");
											final int miny = res.getInt("miny");
											final int minz = res.getInt("minz");
											final int maxx = res.getInt("maxx");
											final int maxy = res.getInt("maxy");
											final int maxz = res.getInt("maxz");
											final int type_id = res.getInt("type_id");
											final float velocity = res.getFloat("velocity");
											final String world = res.getString("world");
											final String owner = res.getString("owner");
											final String name = res.getString("name");
											final String flags = res.getString("flags");
											final String packed_allowed = res.getString("packed_allowed");
											final long last_used = res.getLong("last_used");

											final Field field = new Field(x, y, z, minx, miny, minz, maxx, maxy, maxz, velocity, world, type_id, owner, name, last_used);
											field.setPackedAllowed(packed_allowed);

											final Field parentField = out.get(parent);

											if (parentField != null)
												{
													field.setParent(parentField);
													parentField.addChild(field);
												} else
												{
													field.markForDeletion();
													offerField(field);
												}

											field.setId(id);

											final FieldSettings fs = plugin.getSettingsManager().getFieldSettings(field);

											if (field.getAgeInDays() > plugin.getSettingsManager().getPurgeSnitchAfterDays())
												{
													if (fs != null && fs.hasDefaultFlag(FieldFlag.SNITCH))
														{
															deleteSnitchEntries(field);
															field.markForDeletion();
															offerField(field);
															purged++;
															continue;
														}
												}

											if (fs != null)
												{
													field.setSettings(fs);
													field.setFlags(flags);
													out.put(id, field);

													final PlayerData data = plugin.getPlayerManager().getPlayerData(owner);
													data.incrementFieldCount(type_id);
												}
										} catch (final Exception ex)
										{
											ex.printStackTrace();
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			if (purged > 0)
				{
					PreciousStones.log("({0}) cuboids purged: {1}", worldName, purged);
				}

			return out.values();
		}

	/**
	 * Retrieves all players from the database
	 * 
	 * @param worldName
	 * @return
	 */
	public void extractPlayers()
		{
			int purged = 0;

			final String query = "SELECT * FROM pstone_players;";

			final ResultSet res = core.select(query);

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final String name = res.getString("player_name");
											final long last_seen = res.getLong("last_seen");
											final String flags = res.getString("flags");

											if (last_seen > 0)
												{
													final int lastSeenDays = (int) Dates.differenceInDays(new Date(), new Date(last_seen));

													if (lastSeenDays > plugin.getSettingsManager().getPurgeAfterDays())
														{
															deleteFields(name);
															offerPlayer(name, false);
															purged++;
															continue;
														}
												}

											final PlayerData data = plugin.getPlayerManager().getPlayerData(name);
											data.setFlags(flags);
										} catch (final Exception ex)
										{
											PreciousStones.getLogger().info(ex.getMessage());
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			if (purged > 0)
				{
					PreciousStones.log("players purged: {0}", purged);
				}
		}

	/**
	 * Retrieves all unbreakables belonging to a worlds from the database
	 * 
	 * @param worldName
	 * @return
	 */
	public List<Unbreakable> getUnbreakables(final String worldName)
		{
			final List<Unbreakable> out = new ArrayList<Unbreakable>();
			int purged = 0;

			final String query = "SELECT * FROM  `pstone_unbreakables` LEFT JOIN pstone_players ON pstone_unbreakables.owner = pstone_players.player_name WHERE world = '" + Helper.escapeQuotes(worldName) + "';";

			final ResultSet res = core.select(query);

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final int x = res.getInt("x");
											final int y = res.getInt("y");
											final int z = res.getInt("z");
											final int type_id = res.getInt("type_id");
											final String world = res.getString("world");
											final String owner = res.getString("owner");
											final long last_seen = res.getLong("last_seen");

											final Unbreakable ub = new Unbreakable(x, y, z, world, type_id, owner);

											if (last_seen > 0)
												{
													final int lastSeenDays = (int) Dates.differenceInDays(new Date(), new Date(last_seen));

													if (lastSeenDays > plugin.getSettingsManager().getPurgeAfterDays())
														{
															offerUnbreakable(ub, false);
															offerPlayer(owner, false);
															purged++;
															continue;
														}
												}

											out.add(ub);
										} catch (final Exception ex)
										{
											PreciousStones.getLogger().info(ex.getMessage());
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			if (purged > 0)
				{
					PreciousStones.log("({0}) unbreakables purged: {1}", worldName, purged);
				}

			return out;
		}

	private void updateGrief(final Field field)
		{
			if (field.isDirty(DirtyFieldReason.GRIEF_BLOCKS))
				{
					final Queue<GriefBlock> grief = field.getGrief();

					for (final GriefBlock gb : grief)
						{
							recordBlockGrief(field, gb);
						}
				}
		}

	private void updateField(final Field field)
		{
			String subQuery = "";

			if (field.isDirty(DirtyFieldReason.OWNER))
				{
					subQuery += "owner = '" + field.getOwner() + "', ";
				}

			if (field.isDirty(DirtyFieldReason.RADIUS))
				{
					subQuery += "radius = " + field.getRadius() + ", ";
				}

			if (field.isDirty(DirtyFieldReason.HEIGHT))
				{
					subQuery += "height = " + field.getHeight() + ", ";
				}

			if (field.isDirty(DirtyFieldReason.VELOCITY))
				{
					subQuery += "velocity = " + field.getVelocity() + ", ";
				}

			if (field.isDirty(DirtyFieldReason.NAME))
				{
					subQuery += "name = '" + Helper.escapeQuotes(field.getName()) + "', ";
				}
			if (field.isDirty(DirtyFieldReason.ALLOWED))
				{
					subQuery += "packed_allowed = '" + Helper.escapeQuotes(field.getPackedAllowed()) + "', ";
				}

			if (field.isDirty(DirtyFieldReason.LASTUSED))
				{
					subQuery += "last_used = " + (new Date()).getTime() + ", ";
				}

			if (field.isDirty(DirtyFieldReason.FLAGS))
				{
					subQuery += "flags = '" + Helper.escapeQuotes(field.getFlagsAsString()) + "', ";
				}

			if (field.isDirty(DirtyFieldReason.DIMENSIONS))
				{
					subQuery += "minx = " + field.getMinx() + ", " + "miny = " + field.getMiny() + ", " + "minz = " + field.getMinz() + ", " + "maxx = " + field.getMaxx() + ", " + "maxy = " + field.getMaxy() + ", " + "maxz = " + field.getMaxz() + ", ";
				}

			if (!subQuery.isEmpty())
				{
					String query = "UPDATE `pstone_fields` SET " + Helper.stripTrailing(subQuery, ", ") + " WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";

					if (field.hasFlag(FieldFlag.CUBOID))
						{
							query = "UPDATE `pstone_cuboids` SET " + Helper.stripTrailing(subQuery, ", ") + " WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + field.getWorld() + "';";
						}

					if (plugin.getSettingsManager().isDebugsql())
						{
							PreciousStones.getLogger().info(query);
						}

					if (!core.execute(query))
						{
							// this can happen when the data didn't change
							// PreciousStones.getLogger().info(query +
							// " error");
						}
				}

			field.clearDirty();
		}

	/**
	 * Insert a field into the database
	 * 
	 * @param field
	 */
	public void insertField(final Field field)
		{
			if (pending.containsValue(field.toVec()))
				{
					processSingleField(pending.get(field.toVec()));
				}

			String query = "INSERT INTO `pstone_fields` (  `x`,  `y`, `z`, `world`, `radius`, `height`, `velocity`, `type_id`, `owner`, `name`, `packed_allowed`, `flags`) ";
			String values = "VALUES ( " + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + Helper.escapeQuotes(field.getWorld()) + "'," + field.getRadius() + "," + field.getHeight() + "," + field.getVelocity() + "," + field.getTypeId() + ",'" + field.getOwner() + "','" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "','" + Helper.escapeQuotes(field.getFlagsAsString()) + "');";

			if (field.hasFlag(FieldFlag.CUBOID))
				{
					query = "INSERT INTO `pstone_cuboids` ( `parent`, `x`,  `y`, `z`, `world`, `minx`, `miny`, `minz`, `maxx`, `maxy`, `maxz`, `velocity`, `type_id`, `owner`, `name`, `packed_allowed`, `flags`) ";
					values = "VALUES ( " + (field.getParent() == null ? 0 : field.getParent().getId()) + "," + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + Helper.escapeQuotes(field.getWorld()) + "'," + field.getMinx() + "," + field.getMiny() + "," + field.getMinz() + "," + field.getMaxx() + "," + field.getMaxy() + "," + field.getMaxz() + "," + field.getVelocity() + "," + field.getTypeId() + ",'" + field.getOwner() + "','" + Helper.escapeQuotes(field.getName()) + "','" + Helper.escapeQuotes(field.getPackedAllowed()) + "','" + Helper.escapeQuotes(field.getFlagsAsString()) + "');";
				}

			if (plugin.getSettingsManager().isDebugsql())
				{
					PreciousStones.getLogger().info(query + values);
				}

			synchronized (this)
				{
					field.setId(core.insert(query + values));
				}
		}

	/**
	 * Delete a field from the database
	 * 
	 * @param field
	 */
	public void deleteField(final Field field)
		{
			String query = "DELETE FROM `pstone_fields` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";

			if (field.hasFlag(FieldFlag.CUBOID))
				{
					query = "DELETE FROM `pstone_cuboids` WHERE x = " + field.getX() + " AND y = " + field.getY() + " AND z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";
				}

			core.delete(query);
		}

	/**
	 * Delete a field from the database that a player owns
	 * 
	 * @param playerName
	 */
	public void deleteFields(final String playerName)
		{
			final String query = "DELETE FROM `pstone_fields` WHERE owner = '" + playerName + "';";
			core.delete(query);
		}

	/**
	 * Insert an unbreakable into the database
	 * 
	 * @param ub
	 */
	public void insertUnbreakable(final Unbreakable ub)
		{
			final String query = "INSERT INTO `pstone_unbreakables` (  `x`,  `y`, `z`, `world`, `owner`, `type_id`) ";
			final String values = "VALUES ( " + ub.getX() + "," + ub.getY() + "," + ub.getZ() + ",'" + Helper.escapeQuotes(ub.getWorld()) + "','" + ub.getOwner() + "'," + ub.getTypeId() + ");";
			core.insert(query + values);
		}

	/**
	 * Delete an unbreakable from the database
	 * 
	 * @param ub
	 */
	public void deleteUnbreakable(final Unbreakable ub)
		{
			final String query = "DELETE FROM `pstone_unbreakables` WHERE x = " + ub.getX() + " AND y = " + ub.getY() + " AND z = " + ub.getZ() + " AND world = '" + Helper.escapeQuotes(ub.getWorld()) + "';";
			core.delete(query);
		}

	/**
	 * Insert snitch entry into the database
	 * 
	 * @param snitch
	 * @param se
	 */
	public void insertSnitchEntry(final Field snitch, final SnitchEntry se)
		{
			if (plugin.getSettingsManager().isUseMysql())
				{
					final String query = "INSERT INTO `pstone_snitches` (`x`, `y`, `z`, `world`, `name`, `reason`, `details`, `count`) ";
					final String values = "VALUES ( " + snitch.getX() + "," + snitch.getY() + "," + snitch.getZ() + ",'" + Helper.escapeQuotes(snitch.getWorld()) + "','" + Helper.escapeQuotes(se.getName()) + "','" + Helper.escapeQuotes(se.getReason()) + "','" + Helper.escapeQuotes(se.getDetails()) + "',1) ";
					final String update = "ON DUPLICATE KEY UPDATE count = count+1;";
					core.insert(query + values + update);
				} else
				{
					final String query = "INSERT OR IGNORE INTO `pstone_snitches` (`x`, `y`, `z`, `world`, `name`, `reason`, `details`, `count`) ";
					final String values = "VALUES ( " + snitch.getX() + "," + snitch.getY() + "," + snitch.getZ() + ",'" + Helper.escapeQuotes(snitch.getWorld()) + "','" + Helper.escapeQuotes(se.getName()) + "','" + Helper.escapeQuotes(se.getReason()) + "','" + Helper.escapeQuotes(se.getDetails()) + "',1);";
					final String update = "UPDATE `pstone_snitches` SET count = count+1;";
					core.insert(query + values + update);
				}
		}

	/**
	 * Delete all snitch entries for a snitch form the database
	 * 
	 * @param snitch
	 */
	public void deleteSnitchEntries(final Field snitch)
		{
			final String query = "DELETE FROM `pstone_snitches` WHERE x = " + snitch.getX() + " AND y = " + snitch.getY() + " AND z = " + snitch.getZ() + " AND world = '" + Helper.escapeQuotes(snitch.getWorld()) + "';";

			if (plugin.getSettingsManager().isDebugsql())
				{
					PreciousStones.getLogger().info(query);
				}

			synchronized (this)
				{
					core.delete(query);
				}
		}

	/**
	 * Retrieves all snitches belonging to a worlds from the database
	 * 
	 * @param snitch
	 * @return
	 */
	public List<SnitchEntry> getSnitchEntries(final Field snitch)
		{
			final List<SnitchEntry> workingSnitchEntries = new LinkedList<SnitchEntry>();

			synchronized (pendingSnitchEntries)
				{
					workingSnitchEntries.addAll(pendingSnitchEntries);
					pendingSnitchEntries.clear();
				}

			synchronized (this)
				{
					processSnitches(workingSnitchEntries);
				}

			final List<SnitchEntry> out = new ArrayList<SnitchEntry>();

			final String query = "SELECT * FROM  `pstone_snitches` WHERE x = " + snitch.getX() + " AND y = " + snitch.getY() + " AND z = " + snitch.getZ() + " AND world = '" + Helper.escapeQuotes(snitch.getWorld()) + "' ORDER BY `id` DESC;";

			ResultSet res;

			synchronized (this)
				{
					res = core.select(query);
				}

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final String name = res.getString("name");
											final String reason = res.getString("reason");
											final String details = res.getString("details");
											final int count = res.getInt("count");

											final SnitchEntry ub = new SnitchEntry(null, name, reason, details, count);

											out.add(ub);
										} catch (final Exception ex)
										{
											PreciousStones.getLogger().info(ex.getMessage());
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			return out;
		}

	/**
	 * Delete a player from the players table
	 * 
	 * @param playerName
	 */
	public void deletePlayer(final String playerName)
		{
			final String query = "DELETE FROM `pstone_players` WHERE player_name = '" + playerName + "';";
			core.delete(query);
		}

	/**
	 * Update the player's last seen date on the database
	 * 
	 * @param playerName
	 */
	public void updatePlayer(final String playerName)
		{
			final long time = (new Date()).getTime();

			final PlayerData data = plugin.getPlayerManager().getPlayerData(playerName);

			if (plugin.getSettingsManager().isUseMysql())
				{
					final String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
					final String values = "VALUES ( '" + playerName + "', " + time + ",'" + Helper.escapeQuotes(data.getFlags()) + "') ";
					final String update = "ON DUPLICATE KEY UPDATE last_seen = " + time + ", flags = '" + Helper.escapeQuotes(data.getFlags()) + "';";
					core.insert(query + values + update);
				} else
				{
					final String query = "INSERT OR IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`, `flags`) ";
					final String values = "VALUES ( '" + playerName + "'," + time + ",'" + Helper.escapeQuotes(data.getFlags()) + "');";
					final String update = "UPDATE `pstone_players` SET last_seen = " + time + ", flags = '" + Helper.escapeQuotes(data.getFlags()) + "';";
					core.insert(query + values + update);
				}
		}

	private void touchAllPlayers()
		{
			final long time = (new Date()).getTime();

			if (plugin.getSettingsManager().isUseMysql())
				{
					String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`) ";
					String values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_fields ";
					core.insert(query + values);

					query = "INSERT IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`) ";
					values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_unbreakables ";
					core.insert(query + values);
				} else
				{
					String query = "INSERT INTO `pstone_players` ( `player_name`,  `last_seen`) ";
					String values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_fields ";
					core.insert(query + values);

					query = "INSERT OR IGNORE INTO `pstone_players` ( `player_name`,  `last_seen`) ";
					values = "SELECT DISTINCT `owner`, " + time + " as last_seen FROM pstone_unbreakables ";
					core.insert(query + values);
				}
		}

	/**
	 * Record a single block grief
	 * 
	 * @param field
	 * @param gb
	 */
	public void recordBlockGrief(final Field field, final GriefBlock gb)
		{
			final World world = plugin.getServer().getWorld(gb.getWorld());

			if (world == null)
				{
					return;
				}

			if (!plugin.getGriefUndoManager().isDependentBlock(gb.getTypeId()))
				{
					final BlockFace[] faces =
						{ BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };

					for (final BlockFace face : faces)
						{
							final Block block = world.getBlockAt(gb.getLocation());
							final Block rel = block.getRelative(face);

							if (plugin.getGriefUndoManager().isDependentBlock(rel.getTypeId()))
								{
									recordBlockGrief(field, new GriefBlock(rel.getLocation(), rel.getTypeId(), rel.getData()));
									rel.setTypeId(0);
								}
						}
				}

			if (gb.getTypeId() == 64 || gb.getTypeId() == 71)
				{
					// record wood doors in correct order

					final Block block = world.getBlockAt(gb.getLocation());

					if ((gb.getData() & 0x8) == 0x8)
						{
							final Block bottom = block.getRelative(BlockFace.DOWN);

							recordBlockGriefClean(field, new GriefBlock(bottom));
							recordBlockGriefClean(field, gb);

							bottom.setTypeId(0);
							block.setTypeId(0);
						} else
						{
							final Block top = block.getRelative(BlockFace.UP);

							recordBlockGriefClean(field, gb);
							recordBlockGriefClean(field, new GriefBlock(top));

							block.setTypeId(0);
							top.setTypeId(0);
						}
				} else
				{
					recordBlockGriefClean(field, gb);
				}
		}

	/**
	 * Record a single block grief
	 * 
	 * @param field
	 * @param gb
	 */
	public void recordBlockGriefClean(final Field field, final GriefBlock gb)
		{
			final String query = "INSERT INTO `pstone_grief_undo` ( `date_griefed`, `field_x`, `field_y` , `field_z`, `world`, `x` , `y`, `z`, `type_id`, `data`, `sign_text`) ";
			final String values = "VALUES ( '" + new Timestamp((new Date()).getTime()) + "'," + field.getX() + "," + field.getY() + "," + field.getZ() + ",'" + Helper.escapeQuotes(field.getWorld()) + "'," + gb.getX() + "," + gb.getY() + "," + gb.getZ() + "," + gb.getTypeId() + "," + gb.getData() + ",'" + Helper.escapeQuotes(gb.getSignText()) + "');";
			core.insert(query + values);
		}

	/**
	 * Restores a field's griefed blocks
	 * 
	 * @param field
	 * @return
	 */
	public Queue<GriefBlock> retrieveBlockGrief(final Field field)
		{
			final Set<Field> workingGrief = new HashSet<Field>();

			synchronized (pendingGrief)
				{
					workingGrief.addAll(pendingGrief);
					pendingGrief.clear();
				}

			synchronized (this)
				{
					processGrief(workingGrief);
				}

			final Queue<GriefBlock> out = new LinkedList<GriefBlock>();

			final String query = "SELECT * FROM  `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "' ORDER BY y ASC;";

			if (plugin.getSettingsManager().isDebugsql())
				{
					PreciousStones.getLogger().info(query);
				}

			ResultSet res;

			synchronized (this)
				{
					res = core.select(query);
				}

			if (res != null)
				{
					try
						{
							while (res.next())
								{
									try
										{
											final int x = res.getInt("x");
											final int y = res.getInt("y");
											final int z = res.getInt("z");
											final int type_id = res.getInt("type_id");
											final byte data = res.getByte("data");
											final String signText = res.getString("sign_text");

											final GriefBlock bg = new GriefBlock(x, y, z, field.getWorld(), type_id, data);

											bg.setSignText(signText);
											out.add(bg);
										} catch (final Exception ex)
										{
											PreciousStones.getLogger().info(ex.getMessage());
										}
								}
						} catch (final SQLException ex)
						{
							Logger.getLogger(StorageManager.class.getName()).log(Level.SEVERE, null, ex);
						}
				}

			deleteBlockGrief(field);
			field.clearGrief();
			return out;
		}

	/**
	 * Deletes all records from a specific field
	 * 
	 * @param field
	 */
	public void deleteBlockGrief(final Field field)
		{
			final String query = "DELETE FROM `pstone_grief_undo` WHERE field_x = " + field.getX() + " AND field_y = " + field.getY() + " AND field_z = " + field.getZ() + " AND world = '" + Helper.escapeQuotes(field.getWorld()) + "';";

			if (plugin.getSettingsManager().isDebugsql())
				{
					PreciousStones.getLogger().info(query);
				}
			synchronized (this)
				{
					core.delete(query);
				}
		}

	/**
	 * Deletes all records from a specific block
	 * 
	 * @param block
	 */
	public void deleteBlockGrief(final Block block)
		{
			final String query = "DELETE FROM `pstone_grief_undo` WHERE x = " + block.getX() + " AND y = " + block.getY() + " AND z = " + block.getZ() + " AND world = '" + block.getWorld().getName() + "';";

			if (plugin.getSettingsManager().isDebugsql())
				{
					PreciousStones.getLogger().info(query);
				}
			synchronized (this)
				{
					core.delete(query);
				}
		}

	/**
	 * Schedules the pending queue on save frequency
	 * 
	 * @return
	 */
	public int saverScheduler()
		{
			return plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable()
				{
					public void run()
						{
							if (plugin.getSettingsManager().isDebugsql())
								{
									PreciousStones.getLogger().info("[Queue] processing queue...");
								}
							processQueue();
						}
				}, 0, 20L * plugin.getSettingsManager().getSaveFrequency());
		}

	/**
	 * Process entire queue
	 */
	public void processQueue()
		{
			final Map<Vec, Field> working = new HashMap<Vec, Field>();
			final Map<Unbreakable, Boolean> workingUb = new HashMap<Unbreakable, Boolean>();
			final Map<String, Boolean> workingPlayers = new HashMap<String, Boolean>();
			final Set<Field> workingGrief = new HashSet<Field>();
			final List<SnitchEntry> workingSnitchEntries = new LinkedList<SnitchEntry>();

			synchronized (pending)
				{
					working.putAll(pending);
					pending.clear();
				}
			synchronized (pendingUb)
				{
					workingUb.putAll(pendingUb);
					pendingUb.clear();
				}
			synchronized (pendingGrief)
				{
					workingGrief.addAll(pendingGrief);
					pendingGrief.clear();
				}
			synchronized (pendingPlayers)
				{
					workingPlayers.putAll(pendingPlayers);
					pendingPlayers.clear();
				}
			synchronized (pendingSnitchEntries)
				{
					workingSnitchEntries.addAll(pendingSnitchEntries);
					pendingSnitchEntries.clear();
				}

			synchronized (this)
				{
					processFields(working);
					processUnbreakable(workingUb);
					processGrief(workingGrief);
					processPlayers(workingPlayers);
					processSnitches(workingSnitchEntries);
				}
		}

	/**
	 * Process suingle field
	 * 
	 * @param working
	 */
	public void processSingleField(final Field field)
		{
			if (plugin.getSettingsManager().isDebugdb())
				{
					PreciousStones.getLogger().info("[Queue] processing single query");
				}

			if (field.isDirty(DirtyFieldReason.DELETE))
				{
					deleteField(field);
				} else
				{
					updateField(field);
				}

			pending.remove(field.toVec());
		}

	/**
	 * Process pending pstones
	 * 
	 * @param working
	 */
	public void processFields(final Map<Vec, Field> working)
		{
			if (plugin.getSettingsManager().isDebugdb() && !working.isEmpty())
				{
					PreciousStones.getLogger().info("[Queue] processing " + working.size() + " pstone queries...");
				}

			for (final Field field : working.values())
				{
					if (field.isDirty(DirtyFieldReason.DELETE))
						{
							deleteField(field);
						} else
						{
							updateField(field);
						}
				}
		}

	/**
	 * Process pending grief
	 * 
	 * @param workingUb
	 */
	public void processUnbreakable(final Map<Unbreakable, Boolean> workingUb)
		{
			if (plugin.getSettingsManager().isDebugdb() && !workingUb.isEmpty())
				{
					PreciousStones.getLogger().info("[Queue] processing " + workingUb.size() + " unbreakable queries...");
				}

			for (final Unbreakable ub : workingUb.keySet())
				{
					if (workingUb.get(ub))
						{
							insertUnbreakable(ub);
						} else
						{
							deleteUnbreakable(ub);
						}
				}
		}

	/**
	 * Process pending players
	 * 
	 * @param workingPlayers
	 */
	public void processPlayers(final Map<String, Boolean> workingPlayers)
		{
			if (plugin.getSettingsManager().isDebugdb() && !workingPlayers.isEmpty())
				{
					PreciousStones.getLogger().info("[Queue] processing " + workingPlayers.size() + " player queries...");
				}

			for (final String playerName : workingPlayers.keySet())
				{
					if (workingPlayers.get(playerName))
						{
							updatePlayer(playerName);
						} else
						{
							deletePlayer(playerName);
						}
				}
		}

	/**
	 * Process pending snitches
	 * 
	 * @param workingSnitchEntries
	 */
	public void processSnitches(final List<SnitchEntry> workingSnitchEntries)
		{
			if (plugin.getSettingsManager().isDebugdb() && !workingSnitchEntries.isEmpty())
				{
					PreciousStones.getLogger().info("[Queue] sending " + workingSnitchEntries.size() + " snitch queries...");
				}

			for (final SnitchEntry se : workingSnitchEntries)
				{
					insertSnitchEntry(se.getField(), se);
				}
		}

	/**
	 * Process pending grief
	 * 
	 * @param workingGrief
	 */
	public void processGrief(final Set<Field> workingGrief)
		{
			if (plugin.getSettingsManager().isDebugdb() && !workingGrief.isEmpty())
				{
					PreciousStones.getLogger().info("[Queue] processing " + workingGrief.size() + " grief queries...");
				}

			for (final Field field : workingGrief)
				{
					updateGrief(field);
				}
		}
}
